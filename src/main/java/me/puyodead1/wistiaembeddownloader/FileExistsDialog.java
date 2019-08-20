package me.puyodead1.wistiaembeddownloader;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class FileExistsDialog extends Dialog {

	protected Object result;
	protected Shell shlFileExists;
	private Text txtFileName;
	private Text txtPath;
	private static Button btnOverwrite;
	
	private String fileName, outputPath;
	
	public static Button getOverwriteBtn() {
		return btnOverwrite;
	}

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param string2 
	 * @param string 
	 */
	public FileExistsDialog(Shell parent, int style, String fileName, String outputPath) {
		super(parent, style);
		setText("SWT Dialog");
		this.fileName = fileName;
		this.outputPath = outputPath;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlFileExists.open();
		shlFileExists.layout();
		Display display = getParent().getDisplay();
		while (!shlFileExists.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlFileExists = new Shell(getParent(), SWT.BORDER);
		shlFileExists.setSize(450, 300);
		
		Label lblExists = new Label(shlFileExists, SWT.CENTER);
		lblExists.setFont(SWTResourceManager.getFont("Segoe UI", 18, SWT.BOLD));
		lblExists.setBounds(10, 10, 411, 44);
		lblExists.setText("File exists!");
		
		txtFileName = new Text(shlFileExists, SWT.BORDER);
		txtFileName.setBounds(94, 60, 327, 21);
		txtFileName.setText(fileName);
		
		txtPath = new Text(shlFileExists, SWT.BORDER);
		txtPath.setBounds(94, 88, 327, 21);
		txtPath.setText(outputPath);
		
		Label lblFileName = new Label(shlFileExists, SWT.NONE);
		lblFileName.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblFileName.setBounds(10, 58, 78, 18);
		lblFileName.setText("File name:");
		
		Label lblPath = new Label(shlFileExists, SWT.NONE);
		lblPath.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblPath.setBounds(10, 86, 78, 21);
		lblPath.setText("Path:");
		
		btnOverwrite = new Button(shlFileExists, SWT.NONE);
		btnOverwrite.setBounds(144, 139, 75, 25);
		btnOverwrite.setText("Overwrite");
		btnOverwrite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				shlFileExists.dispose();
				new Downloader(WistiaEmbedDownloader.getAssetDirectURL(), WistiaEmbedDownloader.getFileSaveName());
				WistiaEmbedDownloader.getBtnDownload().setText("Abort");
			}
		});
		
		Button btnCancel = new Button(shlFileExists, SWT.NONE);
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				shlFileExists.close();
			}
		});
		btnCancel.setBounds(250, 139, 75, 25);
		btnCancel.setText("Cancel");
	}
}
