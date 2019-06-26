package me.puyodead1.WistiaDownloader;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MainWindow implements Runnable {

	private JFrame frmWistiaVideoDownload;
	private JTextField txtURL;
	private JButton btnSubmit;
	private JLabel lblFilePath;
	private JTextField txtFilePath;
	private JTextArea lblNewLabel;
	private JProgressBar progressBar;
	private JLabel percentLabel;

	private int videoCodeIndex;
	private String videoCode;
	private String response;
	private URL getDirectURL;
	private String saveAsName;

	private static final int MAX_BUFFER_SIZE = 1024;

	public final String STATUSES[] = { "Downloading", "Paused", "Complete", "Cancelled", "Error" };

	public final int DOWNLOADING = 0;
	public final int PAUSED = 1;
	public final int COMPLETE = 2;
	public final int CANCELLED = 3;
	public final int ERROR = 4;

	private int size;
	private int downloaded;
	private int status;

	private Thread thread;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmWistiaVideoDownload.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWistiaVideoDownload = new JFrame();
		frmWistiaVideoDownload.setTitle("Wistia Embeded Video Downloader by Puyodead1");
		frmWistiaVideoDownload.setBounds(100, 100, 668, 475);
		frmWistiaVideoDownload.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmWistiaVideoDownload.getContentPane().setLayout(null);
		frmWistiaVideoDownload.setResizable(false);

		JLabel lblPleaseEnterUrl = new JLabel("Enter URL:");
		lblPleaseEnterUrl.setBounds(10, 11, 104, 14);
		frmWistiaVideoDownload.getContentPane().add(lblPleaseEnterUrl);

		txtURL = new JTextField();
		txtURL.setBounds(124, 8, 470, 20);
		frmWistiaVideoDownload.getContentPane().add(txtURL);
		txtURL.setColumns(10);

		btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txtFilePath.getText().isEmpty() || txtURL.getText().isEmpty()) {
					lblNewLabel.append("\nPlease fillout all fields!");
					return;
				}
				videoCodeIndex = txtURL.getText().toString().indexOf("?wvideo=");
				videoCode = txtURL.getText().toString().substring(videoCodeIndex + 8, videoCodeIndex + 18);
				try {
					URL url = new URL("https://fast.wistia.com/embed/medias/" + videoCode + ".json");
					URLConnection request = url.openConnection();
					request.connect();

					JsonParser jp = new JsonParser();
					JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
					JsonObject rootobj = root.getAsJsonObject();
					JsonObject mediaObj = rootobj.get("media").getAsJsonObject();
					JsonArray assetArray = mediaObj.get("assets").getAsJsonArray();

					ArrayList<String> assetList = new ArrayList<String>();
					if (assetArray != null) {
						int len = assetArray.size();
						for (int i = 0; i < len; i++) {
							// these are all the different qualities
							assetList.add(assetArray.get(i).toString());

							// get slug: mp4_h264_1076k, 10th one (9 in array)
						}
						String asset = assetList.get(9);

						JsonElement assetRoot = jp.parse(asset);
						JsonObject assetObj = assetRoot.getAsJsonObject();
						String assetURL = assetObj.get("url").getAsString();

						response = assetURL;
					}

					getDirectURL = new URL("https://embed-ssl.wistia.com/deliveries/" + response + ".mp4");

					String[] a = txtURL.getText().toString().split("/lessons/");
					String b = a[a.length - 1];
					String videoTitle = b.split("\\?")[0];

					saveAsName = txtFilePath.getText().toString() + File.separator + videoTitle + ".mp4";
					lblNewLabel.append("\nFile will be downloaded to: " + txtFilePath.getText().toString());
					lblNewLabel.append("\nFile will be saved as: " + videoTitle + ".mp4");
					copyURLToFile();

					progressBar.setValue(0);
				} catch (IOException e1) {
					lblNewLabel.append("\nERROR: " + e1.getMessage());
				}
			}
		});
		btnSubmit.setBounds(247, 67, 89, 23);
		frmWistiaVideoDownload.getContentPane().add(btnSubmit);

		lblFilePath = new JLabel("Output folder:");
		lblFilePath.setBounds(10, 39, 89, 14);
		frmWistiaVideoDownload.getContentPane().add(lblFilePath);

		txtFilePath = new JTextField();
		txtFilePath.setColumns(10);
		txtFilePath.setBounds(125, 36, 469, 20);
		frmWistiaVideoDownload.getContentPane().add(txtFilePath);

		JScrollPane scrollPane = new JScrollPane(lblNewLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane.setBounds(10, 161, 641, 276);
		frmWistiaVideoDownload.getContentPane().add(scrollPane);

		lblNewLabel = new JTextArea("");
		scrollPane.setViewportView(lblNewLabel);
		lblNewLabel.setEditable(false);

		percentLabel = new JLabel("0%");
		percentLabel.setBounds(286, 138, 50, 24);
		frmWistiaVideoDownload.getContentPane().add(percentLabel);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 98, 641, 41);
		progressBar.setValue(0);
		frmWistiaVideoDownload.getContentPane().add(progressBar);

	}

	public void copyURLToFile() {
		btnSubmit.setEnabled(false);
		lblNewLabel.append("\nDownloading...");
		size = -1;
		downloaded = 0;
		status = DOWNLOADING;

		if (this.getDirectURL != null && this.saveAsName != null)
			download();
		else
			lblNewLabel.append("\nERROR: getDirectURL or saveAsName is null, report this");
	}

	public int getSize() {
		return size;
	}

	public float getProgress() {
		return ((float) downloaded / size) * 100;
	}

	public int getStatus() {
		return status;
	}

	private void error() {
		status = ERROR;
		stateChanged();
	}

	// from stack overflow
	private void download() {
		thread = new Thread(this);
		thread.start();

		// Thread updateProgress = new Thread(this);
		// updateProgress.start();
	}

	public void run() {
		RandomAccessFile file = null;
		InputStream stream = null;
		try {
			File folder = new File(txtFilePath.getText().toString());
			if (!folder.exists()) {
				folder.mkdir();
				lblNewLabel.append("\nOutput directory doesnt exist, creating!");
			} else if (folder.isFile()) {
				lblNewLabel.append("\nOutput directory is a file!");
				thread.interrupt();
				return;
			}

			// Open connection to URL.
			HttpURLConnection connection = (HttpURLConnection) getDirectURL.openConnection();

			// Specify what portion of file to download.
			connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

			// Connect to server.
			connection.connect();

			// Make sure response code is in the 200 range.
			if (connection.getResponseCode() / 100 != 2) {
				error();
			}

			// Check for valid content length.
			int contentLength = connection.getContentLength();
			if (contentLength < 1) {
				error();
			}

			/*
			 * Set the size for this download if it hasn't been already set.
			 */
			if (size == -1) {
				size = contentLength;
				stateChanged();
			}

			// Open file and seek to the end of it.
			file = new RandomAccessFile(saveAsName, "rw");
			file.seek(downloaded);

			stream = connection.getInputStream();
			while (status == DOWNLOADING) {
				/*
				 * Size buffer according to how much of the file is left to download.
				 */
				byte buffer[];
				if (size - downloaded > MAX_BUFFER_SIZE) {
					buffer = new byte[MAX_BUFFER_SIZE];
				} else {
					buffer = new byte[size - downloaded];
				}

				// Read from server into buffer.
				int read = stream.read(buffer);
				if (read == -1)
					break;

				// Write buffer to file.
				file.write(buffer, 0, read);
				downloaded += read;
				stateChanged();
			}

			/*
			 * Change status to complete if this point was reached because downloading has
			 * finished.
			 */
			if (status == DOWNLOADING) {
				status = COMPLETE;
				stateChanged();
				lblNewLabel.append("\nDownload Complete\n");
				btnSubmit.setEnabled(true);
			}
		} catch (Exception e) {
			error();
			lblNewLabel.append("\nERROR: " + e.getMessage());
		} finally {
			// Close file.
			if (file != null) {
				try {
					file.close();
				} catch (Exception e) {
				}
			}

			// Close connection to server.
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public void stateChanged() {
		if (status == DOWNLOADING) {

			int progress = Math.round(getProgress());
			progressBar.setValue(Math.round(getProgress()));

			percentLabel.setText(progress + "%");
		}
	}
}
