/*
 * Copyright 2004 - 2013 Wayne Grant
 *           2013 - 2015 Kai Kramer
 *
 * This file is part of KeyStore Explorer.
 *
 * KeyStore Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyStore Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyStore Explorer.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.keystore_explorer.gui.error;

import static java.awt.Dialog.ModalityType.DOCUMENT_MODAL;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.sf.keystore_explorer.gui.CursorUtil;
import net.sf.keystore_explorer.gui.JEscDialog;
import net.sf.keystore_explorer.gui.PlatformUtil;

/**
 * Displays an error message with the option to display the stack trace.
 * 
 */
public class DError extends JEscDialog {
	private static ResourceBundle res = ResourceBundle.getBundle("net/sf/keystore_explorer/gui/error/resources");

	private JPanel jpError;
	private JLabel jlError;
	private JPanel jpButtons;
	private JButton jbDetails;
	private JButton jbOK;

	private Throwable error;

	/**
	 * Creates new DError dialog where the parent is a frame.
	 * 
	 * @param modality
	 *            Create the dialog as modal?
	 * @param parent
	 *            Parent frame
	 * @param error
	 *            Error to display
	 */
	public DError(JFrame parent, Dialog.ModalityType modality, Throwable error) {
		super(parent, res.getString("DError.Title"), modality);
		this.error = error;
		initComponents();
	}

	/**
	 * Creates new DError dialog where the parent is a dialog.
	 * 
	 * @param parent
	 *            Parent dialog
	 * @param modality
	 *            Create the dialog as modal?
	 * @param error
	 *            Error to display
	 */
	public DError(JDialog parent, Dialog.ModalityType modality, Throwable error) {
		super(parent, res.getString("DError.Title"), modality);
		this.error = error;
		initComponents();
	}

	/**
	 * Creates new DError dialog where the parent is a frame.
	 * 
	 * @param modality
	 *            Create the dialog as modal?
	 * @param title
	 *            Dialog title
	 * @param parent
	 *            Parent frame
	 * @param error
	 *            Error to display
	 */
	public DError(JFrame parent, String title, Dialog.ModalityType modality, Throwable error) {
		super(parent, modality);
		setTitle(title);
		this.error = error;
		initComponents();
	}

	/**
	 * Creates new DError dialog where the parent is a dialog.
	 * 
	 * @param parent
	 *            Parent dialog
	 * @param title
	 *            Dialog title
	 * @param modality
	 *            Create the dialog as modal?
	 * @param error
	 *            Error to display
	 */
	public DError(JDialog parent, String title, Dialog.ModalityType modality, Throwable error) {
		super(parent, modality);
		setTitle(title);
		this.error = error;
		initComponents();
	}

	/**
	 * Display an error for the supplied frame as application modal.
	 * 
	 * @param frame
	 *            Frame
	 * @param error
	 *            Error
	 */
	public static void displayError(JFrame frame, Throwable error) {
		DError dError = new DError(frame, DOCUMENT_MODAL, error);
		dError.setLocationRelativeTo(frame);
		dError.setVisible(true);
	}

	/**
	 * Display an error for the supplied dialog as application modal.
	 * 
	 * @param dialog
	 *            Dialog
	 * @param error
	 *            Error
	 */
	public static void displayError(JDialog dialog, Throwable error) {
		DError dError = new DError(dialog, DOCUMENT_MODAL, error);
		dError.setLocationRelativeTo(dialog);
		dError.setVisible(true);
	}

	private void initComponents() {
		jpError = new JPanel(new FlowLayout(FlowLayout.CENTER));
		jpError.setBorder(new EmptyBorder(5, 5, 5, 5));

		jlError = new JLabel(formatError());
		ImageIcon icon = new ImageIcon(getClass().getResource(res.getString("DError.Error.image")));
		jlError.setIcon(icon);
		jlError.setHorizontalTextPosition(SwingConstants.TRAILING);
		jlError.setIconTextGap(15);

		jpError.add(jlError);

		// Buttons
		jbDetails = new JButton(res.getString("DError.jbDetails.text"));
		PlatformUtil.setMnemonic(jbDetails, res.getString("DError.jbDetails.mnemonic").charAt(0));

		jbDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					CursorUtil.setCursorBusy(DError.this);
					showErrorDetail();
				} finally {
					CursorUtil.setCursorFree(DError.this);
				}
			}
		});

		jbOK = new JButton(res.getString("DError.jbOK.text"));
		jbOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				okPressed();
			}
		});

		jpButtons = PlatformUtil.createDialogButtonPanel(jbOK, null, jbDetails, false);

		getContentPane().add(jpError, BorderLayout.CENTER);
		getContentPane().add(jpButtons, BorderLayout.SOUTH);

		setResizable(false);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		getRootPane().setDefaultButton(jbOK);

		pack();
	}

	private String formatError() {
		String message = error.getMessage();

		if (message != null) {
			return MessageFormat.format("<html>{0}:<br>{1}</html>", error.getClass().getName(), breakLine(message, 50));
		} else {
			return error.getClass().getName();
		}
	}

	private String breakLine(String line, int maxLineLength) {
		StringBuffer sb = new StringBuffer();

		StringTokenizer strTok = new StringTokenizer(line, " ");

		String currentLine = "";

		while (strTok.hasMoreTokens()) {
			String word = strTok.nextToken();

			if (currentLine.length() == 0) {
				currentLine += word;
				continue;
			}

			if ((currentLine.length() + word.length() + 1) <= maxLineLength) {
				currentLine += " ";
				currentLine += word;
				continue;
			} else {
				if (sb.length() > 0) {
					sb.append("<br>");
				}

				sb.append(currentLine);
				currentLine = word;
			}
		}

		if (sb.length() > 0) {
			sb.append("<br>");
		}

		sb.append(currentLine);

		return sb.toString();
	}

	private void showErrorDetail() {
		DErrorDetail dErrorDetail = new DErrorDetail(this, DOCUMENT_MODAL, error);
		dErrorDetail.setLocationRelativeTo(this);
		dErrorDetail.setVisible(true);
	}

	private void okPressed() {
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
}