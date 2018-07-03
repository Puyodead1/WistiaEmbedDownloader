package me.puyodead1.WistiaDownloader;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.io.IOUtils;

public class MainWindow {

	private static JFrame frmWistiaVideoDownload;
	private static JTextField txtURL;
	private static JLabel lblVideocode;
	private static JButton btnSubmit;
	private static JLabel lblFilePath;
	private static JTextField txtFilePath;
	private static JLabel lblNewLabel;
	
	private static int videoCodeIndex;
	private static String videoCode;
	private static URL getDeliveryAddress;
	private static List<String> list;
	private static String response;
	private static URL getDirectURL;
	private static File file;
	
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
		frmWistiaVideoDownload.setTitle("Wistia Video Download by Puyodead1");
		frmWistiaVideoDownload.setBounds(100, 100, 677, 586);
		frmWistiaVideoDownload.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmWistiaVideoDownload.getContentPane().setLayout(null);

		JLabel lblPleaseEnterUrl = new JLabel("Please enter URL:");
		lblPleaseEnterUrl.setBounds(10, 11, 104, 14);
		frmWistiaVideoDownload.getContentPane().add(lblPleaseEnterUrl);

		txtURL = new JTextField();
		txtURL.setBounds(124, 8, 470, 20);
		frmWistiaVideoDownload.getContentPane().add(txtURL);
		txtURL.setColumns(10);

		lblVideocode = new JLabel("");
		lblVideocode.setBounds(31, 116, 527, 41);
		frmWistiaVideoDownload.getContentPane().add(lblVideocode);

		btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				videoCodeIndex = txtURL.getText().toString().indexOf("?wvideo=");
				videoCode = txtURL.getText().toString().substring(videoCodeIndex + 8, videoCodeIndex + 18);
				try {
					getDeliveryAddress = new URL(
							"https://fast.wistia.com/embed/medias/" + videoCode + ".json?callback=wistiajson12");

					list = IOUtils.readLines(getDeliveryAddress.openStream());
					response = list.get(0).substring(277, 317);
					getDirectURL = new URL("https://embed-ssl.wistia.com/deliveries/" + response + ".mp4");

					file = new File(txtFilePath.getText().toString());
					copyURLToFile(getDirectURL, file);
				} catch (IOException e1) {
					System.out.println(e1.getStackTrace());
				}
			}
		});
		btnSubmit.setBounds(269, 116, 89, 23);
		frmWistiaVideoDownload.getContentPane().add(btnSubmit);

		lblFilePath = new JLabel("Enter path to folder:");
		lblFilePath.setBounds(10, 39, 139, 14);
		frmWistiaVideoDownload.getContentPane().add(lblFilePath);

		txtFilePath = new JTextField();
		txtFilePath.setColumns(10);
		txtFilePath.setBounds(125, 36, 469, 20);
		frmWistiaVideoDownload.getContentPane().add(txtFilePath);

		lblNewLabel = new JLabel("");
		lblNewLabel.setBounds(0, 138, 661, 409);
		frmWistiaVideoDownload.getContentPane().add(lblNewLabel);
	}

	public static void copyURLToFile(URL url, File file) {

		try {
			InputStream input = url.openStream();
			if (file.exists()) {
				if (file.isDirectory())
					lblNewLabel.setText("File is a directory");
				if (!file.canWrite())
					lblNewLabel.setText("File cannot be written");
			} else {
				File parent = file.getParentFile();
				if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
					lblNewLabel.setText("File cannot be written");
				}
			}

			FileOutputStream output = new FileOutputStream(file);

			byte[] buffer = new byte[4096];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}

			input.close();
			output.close();

			lblNewLabel.setText("File downloaded successfully!");
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}
}
