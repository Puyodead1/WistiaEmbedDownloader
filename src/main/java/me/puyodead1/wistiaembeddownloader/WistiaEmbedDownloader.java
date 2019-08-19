package me.puyodead1.wistiaembeddownloader;

import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Combo;

public class WistiaEmbedDownloader extends Shell {
	private Text txtURL;
	private Text txtOutputLocation;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
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
	 * @param display
	 */
	public WistiaEmbedDownloader(Display display) {
		super(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);
		
		txtURL = new Text(this, SWT.BORDER);
		txtURL.setBounds(123, 10, 529, 21);
		
		Label lblEnterUrl = new Label(this, SWT.NONE);
		lblEnterUrl.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblEnterUrl.setBounds(10, 13, 93, 15);
		lblEnterUrl.setText("Enter URL:");
		
		Label lblOutputLocation = new Label(this, SWT.NONE);
		lblOutputLocation.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblOutputLocation.setBounds(10, 40, 93, 15);
		lblOutputLocation.setText("Output Location:");
		
		txtOutputLocation = new Text(this, SWT.BORDER);
		txtOutputLocation.setBounds(123, 37, 448, 21);
		txtOutputLocation.setText(Paths.get("").toAbsolutePath().toString());
		
		Button btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				DirectoryDialog browseDialog = new DirectoryDialog(getShell());
				browseDialog.setMessage("Select save location");
				String directory = browseDialog.open();
				if(directory != null) {
					txtOutputLocation.setText(directory);
				}
			}
		});
		btnBrowse.setBounds(577, 35, 75, 25);
		btnBrowse.setText("Browse");
		
		final Button btnDownload = new Button(this, SWT.NONE);
		btnDownload.setBounds(191, 106, 258, 25);
		btnDownload.setText("Download");
		btnDownload.setEnabled(false);
		createContents();
		
		final Button btnGetAvailableQualities = new Button(this, SWT.NONE);
		btnGetAvailableQualities.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				btnGetAvailableQualities.setEnabled(false);
				btnDownload.setEnabled(true); // TODO: only set enabled if we got qualities without error
			}
		});
		btnGetAvailableQualities.setBounds(310, 64, 137, 25);
		btnGetAvailableQualities.setText("Get Available Qualities");
		
		Combo comboQualities = new Combo(this, SWT.NONE);
		comboQualities.setBounds(191, 66, 113, 23);
		comboQualities.setText("Select Quality");
		comboQualities.setEnabled(false);
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
}
