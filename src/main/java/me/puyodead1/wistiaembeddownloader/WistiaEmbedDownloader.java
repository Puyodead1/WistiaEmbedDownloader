package me.puyodead1.wistiaembeddownloader;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WistiaEmbedDownloader extends Shell {

	/**
	 * Common Errors: String Index out of range - Invalid URL/code not found No
	 * assets found - The asset list doesn't contain any assets or invalid video ID
	 * supplied Quality selection combo box empty - Asset list does not contain any
	 * assets or invalid video ID supplied Missing URL - No URL was supplied in the
	 * URL input box Invalid URL - URL does not start with valid http:// or https://
	 */

	private Text txtURL, txtOutputLocation, txtReferer;
	private static StyledText txtConsole;
	private Label lblEnterUrl, lblOutputLocation, lblNoteThatVideo, lblRefererDomain;
	private Button btnBrowse, btnSpoofReferer;
	private static Button btnGetAvailableQualities;
	private static Button btnDownload;
	private static Combo comboQualities;
	private static Display display;
	private static Label lblProgress;
	private static ProgressBar progressBar;

	private String videoID;
	private static String fileSaveName;
	private int videoCodeIndex;
	private URL assetListURL;
	private static URL assetDirectURL;
	private Label lblConsole;

	private static Color RED = new Color(display, 255, 0, 0);
	private static Color BLACK = new Color(display, 0, 0, 0);

	private HashMap<String, String> assets = new HashMap<String, String>();

	public JsonParser parser = new JsonParser();

	public static Label getProgressLbl() {
		return lblProgress;
	}

	public static ProgressBar getProgressBar() {
		return progressBar;
	}

	public static void log(String text) {
		int start = txtConsole.getText().length();
		txtConsole.append(text + "\n");
		StyleRange range = new StyleRange();
		range.start = start;
		range.length = text.length();
		range.foreground = BLACK;
	}

	public static void error(String text) {
		int start = txtConsole.getText().length();
		txtConsole.append(text + "\n");
		StyleRange range = new StyleRange();
		range.start = start;
		range.length = text.length();
		range.foreground = RED;
		txtConsole.setStyleRange(range);
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			display = Display.getDefault();
			WistiaEmbedDownloader shell = new WistiaEmbedDownloader(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public WistiaEmbedDownloader(Display display) {
		super(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);

		lblConsole = new Label(this, SWT.NONE);
		lblConsole.setBackground(new Color(display, 240, 240, 240));
		lblConsole.setBounds(288, 196, 55, 15);
		lblConsole.setText("Console");

		txtURL = new Text(this, SWT.BORDER);
		txtURL.setBounds(131, 10, 521, 21);

		txtConsole = new StyledText(this, SWT.BORDER | SWT.READ_ONLY);
		txtConsole.setBounds(10, 216, 642, 220);

		txtOutputLocation = new Text(this, SWT.BORDER);
		txtOutputLocation.setEditable(true);
		txtOutputLocation.setBounds(131, 37, 440, 21);
		txtOutputLocation.setText(Paths.get("").toAbsolutePath().toString());

		lblProgress = new Label(this, SWT.NONE);
		lblProgress.setBounds(288, 164, 55, 15);
		lblProgress.setText("0%");
		lblProgress.setVisible(false);

		lblEnterUrl = new Label(this, SWT.NONE);
		lblEnterUrl.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblEnterUrl.setBounds(10, 10, 115, 15);
		lblEnterUrl.setText("Enter URL/Video ID:");

		lblOutputLocation = new Label(this, SWT.NONE);
		lblOutputLocation.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblOutputLocation.setBounds(10, 37, 115, 15);
		lblOutputLocation.setText("Output Location:");

		btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				DirectoryDialog browseDialog = new DirectoryDialog(getShell());
				browseDialog.setMessage("Select save location");
				String directory = browseDialog.open();
				if (directory != null) {
					txtOutputLocation.setText(directory);
				}
			}
		});
		btnBrowse.setBounds(577, 35, 75, 25);
		btnBrowse.setText("Browse");

		btnGetAvailableQualities = new Button(this, SWT.NONE);
		btnGetAvailableQualities.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					if (!Strings.isNullOrEmpty(txtURL.getText())) {
						if (txtURL.getText().trim().split("\\|")[0].length() > 10) { // greater then 10 means it is not
																						// just a code
							if (!Strings.isNullOrEmpty(txtConsole.getText())) {
								txtConsole.setText("");
							}
							// Get index of video ID
							videoCodeIndex = txtURL.getText().trim().contains("?wvideo=")
									? txtURL.getText().trim().indexOf("?wvideo=")
									: txtURL.getText().trim().indexOf(";wvideoid=");

							System.out.println("VideoCodeIndex: " + videoCodeIndex);

							// Get Video ID using index
							videoID = txtURL.getText().trim().contains("?wvideo=")
									? txtURL.getText().trim().substring(videoCodeIndex + 8, videoCodeIndex + 18)
									: txtURL.getText().trim().substring(videoCodeIndex + 10, videoCodeIndex + 20);

							// Form the asset list URL
							try {
								assetListURL = new URL("https://fast.wistia.com/embed/medias/" + videoID + ".json");
							} catch (MalformedURLException e1) {
								error(e1.getMessage());
								e1.printStackTrace();
							}

							URLConnection connection = assetListURL.openConnection();
							if(txtReferer.isEnabled()) {
								connection.addRequestProperty("Referer", txtReferer.getText());
							}
							connection.connect();

							JsonElement root = parser
									.parse(new InputStreamReader((InputStream) connection.getContent()));
							JsonObject rootObj = root.getAsJsonObject();
							JsonArray assetArray = rootObj.get("media").getAsJsonObject().get("assets")
									.getAsJsonArray();

							if (assetArray != null) {
								for (JsonElement je : assetArray) {
									JsonObject assetObj = je.getAsJsonObject();
									if (assetObj.get("type").getAsString().contains("mp4")) {
										assets.put(assetObj.get("display_name").getAsString(),
												assetObj.get("url").getAsString());
									}
								}
								System.out.println(assets.size());

								for (String s : assets.keySet()) {
									comboQualities.add(s);
								}
							}

							/*
							 * for(JsonElement je : assetList) { JsonObject assetObj = je.getAsJsonObject();
							 * comboQualities.add(assetObj.get("display_name").getAsString()); }
							 */

							// Check if any assets were actually found and added to the list
							if (comboQualities.getItems().length > 0) {
								btnGetAvailableQualities.setEnabled(false);
								comboQualities.setText(comboQualities.getItem(0));
								log("Please select a quality to download.");
							} else {
								if (!Strings.isNullOrEmpty(txtConsole.getText())) {
									txtConsole.setText("");
								}
								error("No assets found!");
							}
							comboQualities.setEnabled(true);
						} else if (txtURL.getText().trim().split("\\|")[0].length() == 10) { // 10 means it is just a
																								// code (manual input
																								// mode)
							/**
							 * 
							 */
							if (!Strings.isNullOrEmpty(txtConsole.getText())) {
								txtConsole.setText("");
							}
							videoID = txtURL.getText().trim().split("\\|")[0];

							// Form the asset list URL
							try {
								assetListURL = new URL("https://fast.wistia.com/embed/medias/" + videoID + ".json");
							} catch (MalformedURLException e1) {
								error(e1.getMessage());
								e1.printStackTrace();
							}

							URLConnection connection = assetListURL.openConnection();
							if(txtReferer.isEnabled()) {
								connection.addRequestProperty("Referer", txtReferer.getText());
							}
							connection.connect();

							JsonElement root = parser
									.parse(new InputStreamReader((InputStream) connection.getContent()));
							
							JsonObject rootObj = root.getAsJsonObject();
							JsonArray assetArray = rootObj.get("media").getAsJsonObject().get("assets")
									.getAsJsonArray();

							if (assetArray != null) {
								for (JsonElement je : assetArray) {
									JsonObject assetObj = je.getAsJsonObject();
									if (assetObj.get("type").getAsString().contains("mp4")) {
										assets.put(assetObj.get("display_name").getAsString(),
												assetObj.get("url").getAsString());
									}
								}

								for (String s : assets.keySet()) {
									comboQualities.add(s);
								}
							}

							/*
							 * for(JsonElement je : assetList) { JsonObject assetObj = je.getAsJsonObject();
							 * comboQualities.add(assetObj.get("display_name").getAsString()); }
							 */

							// Check if any assets were actually found and added to the list
							if (comboQualities.getItems().length > 0) {
								btnGetAvailableQualities.setEnabled(false);
								comboQualities.setText(comboQualities.getItem(0));
								log("Please select a quality to download.");
							} else {
								if (!Strings.isNullOrEmpty(txtConsole.getText())) {
									txtConsole.setText("");
								}
								error("No assets found!");
							}
							comboQualities.setEnabled(true);
							/**
							 * 
							 */
						} else {
							if (!Strings.isNullOrEmpty(txtConsole.getText())) {
								txtConsole.setText("");
							}
							error("Invalid URL or Video ID!");
						}
					} else {
						if (!Strings.isNullOrEmpty(txtConsole.getText())) {
							txtConsole.setText("");
						}
						error("Missing URL or Video ID!");
					}
				} catch (Throwable th) {
					error("Throwable was cought! " + th.getMessage());
					th.printStackTrace();
				}
			}
		});
		btnGetAvailableQualities.setBounds(311, 70, 137, 25);
		btnGetAvailableQualities.setText("Get Available Qualities");

		comboQualities = new Combo(this, SWT.NONE);
		comboQualities.setBounds(192, 72, 113, 23);
		comboQualities.setText("Select Quality");
		comboQualities.setEnabled(false);
		comboQualities.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				getBtnDownload().setEnabled(true);
			}
		});

		btnDownload = new Button(this, SWT.NONE);
		btnDownload.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO: Make sure this status check works
				if (Downloader.getStatus() == Downloader.DOWNLOADING) {
					Downloader.getThread().stop();
					setupForNewDownload(true);
					error("Thread interrupted at " + Math.round(Downloader.getProgress()) + "%");
				} else if (Downloader.getStatus() == Downloader.INVALID
						|| Downloader.getStatus() == Downloader.COMPLETE) {
					try {
						assetDirectURL = new URL(
								assets.get(comboQualities.getItem(comboQualities.getSelectionIndex())));
						System.out.println(assetListURL);
						System.out.println(getAssetDirectURL());

						String[] a = txtURL.getText().trim().contains("?wvideo=")
								? txtURL.getText().trim().split("\\?wvideo=")[0].split("(/lesson/)|(/lessons/)")
								: txtURL.getText().trim().split(";wvideoid=")[0].split("/lessons/");
						String b = a[a.length - 1];
						String videoTitle = txtURL.getText().trim().split("\\|")[0].length() == 10
								? txtURL.getText().trim().split("|")[1]
								: txtURL.getText().trim().contains("\\?wvideo=") ? a[a.length - 1].split("/")[0]
										: a[a.length - 1].split("/")[0];
						System.out.println(videoTitle);

						fileSaveName = txtOutputLocation.getText().trim() + File.separator + videoTitle + ".mp4";
						log("File will be downloaded to: " + txtOutputLocation.getText().trim());
						log("File will be saved as: " + videoTitle + ".mp4");

						if (getAssetDirectURL() != null || getFileSaveName() != null) {
							File file = new File(getFileSaveName());
							if (!file.exists()) {
								if(txtReferer.isEnabled()) {
									new Downloader(getAssetDirectURL(), getFileSaveName(), txtReferer.getText());
								} else {
									new Downloader(getAssetDirectURL(), getFileSaveName(), null);
								}
								getBtnDownload().setText("Abort");
							} else {
								// TODO: Show overwrite dialog
								final FileExistsDialog dialog;
								if(txtReferer.isEnabled()) {
									dialog = new FileExistsDialog(getShell(), getStyle(),
											videoTitle + ".mp4", txtOutputLocation.getText().trim(), txtReferer.getText());
								} else {
									dialog = new FileExistsDialog(getShell(), getStyle(),
											videoTitle + ".mp4", txtOutputLocation.getText().trim(), null);
								}
								dialog.open();
							}
						} else {
							error("assetDirectURL: " + getAssetDirectURL() + "\n" + "fileSaveName: "
									+ getFileSaveName());
						}
					} catch (MalformedURLException e1) {
						error(e1.getMessage());
						e1.printStackTrace();
					}
				} else {
					error("Downloader state is " + Downloader.getStatus()
							+ ". \n Cannot continue! Try restartng the program.");
				}
			}
		});
		getBtnDownload().setBounds(191, 95, 258, 25);
		getBtnDownload().setText("Download");
		getBtnDownload().setEnabled(false);

		progressBar = new ProgressBar(this, SWT.NONE);
		progressBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		progressBar.setBounds(75, 153, 448, 37);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setVisible(false);

		lblNoteThatVideo = new Label(this, SWT.WRAP);
		lblNoteThatVideo.setBounds(10, 74, 125, 37);
		lblNoteThatVideo.setText("Note: Video IDs are 10 characters long.");
		
		btnSpoofReferer = new Button(this, SWT.CHECK);
		btnSpoofReferer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lblRefererDomain.setEnabled(!txtReferer.isEnabled());
				txtReferer.setEnabled(!txtReferer.isEnabled());
				txtReferer.forceFocus();
			}
		});
		btnSpoofReferer.setBounds(10, 110, 115, 16);
		btnSpoofReferer.setText("Spoof Referer?");
		
		txtReferer = new Text(this, SWT.BORDER);
		txtReferer.setEnabled(false);
		txtReferer.setBounds(10, 153, 94, 21);
		
		lblRefererDomain = new Label(this, SWT.NONE);
		lblRefererDomain.setEnabled(false);
		lblRefererDomain.setAlignment(SWT.CENTER);
		lblRefererDomain.setBounds(10, 132, 94, 15);
		lblRefererDomain.setText("Referer");
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Wistia Embed Downloader");
		setSize(668, 475);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public static URL getAssetDirectURL() {
		return assetDirectURL;
	}

	public static String getFileSaveName() {
		return fileSaveName;
	}

	public static Button getBtnDownload() {
		return btnDownload;
	}

	public static Button getBtnGetAvailableQualities() {
		return btnGetAvailableQualities;
	}

	public static Combo getComboQualities() {
		return comboQualities;
	}

	public static void setupForNewDownload(Boolean clearConsole) {
		getBtnDownload().setText("Download");
		getBtnDownload().setEnabled(false);
		getBtnGetAvailableQualities().setEnabled(true);
		getComboQualities().setEnabled(false);
		getComboQualities().removeAll();
		getComboQualities().setText("Select Quality");
		lblProgress.setVisible(false);
		lblProgress.setText("0%");
		if (clearConsole) {
			txtConsole.setText("");
		}
	}
}
