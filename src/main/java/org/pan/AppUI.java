package org.pan;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class AppUI {

	protected Shell shell;
	private Text text;
	private Text text_1;
	private Spinner spinner;
	private Image image = ImageUtil.get("update_16");
	/**
	 * @wbp.nonvisual location=120,137
	 */
	private final TrayItem trayItem = new TrayItem(Display.getDefault().getSystemTray(), SWT.NONE);

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AppUI window = new AppUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(SWT.CLOSE|SWT.TITLE|SWT.CLOSE);
		shell.setImage(ImageUtil.get("update_48"));
		shell.setSize(450, 180);
		shell.setText("自动监控拷贝");
		shell.setLayout(new GridLayout(3, false));
		
		Label label = new Label(shell, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("监控路径");
		
		text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.setText(AppConfigrator.getMonitorFolder());
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setText(selectFolder());
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setText("浏览");
		btnNewButton.setImage(ImageUtil.get("folder_16"));
		
		Label label_1 = new Label(shell, SWT.NONE);
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_1.setText("对拷路径");
		
		text_1 = new Text(shell, SWT.BORDER);
		text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_1.setText(AppConfigrator.getTargetFolder());
		
		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text_1.setText(selectFolder());
			}
		});
		btnNewButton_1.setText("浏览");
		btnNewButton_1.setImage(ImageUtil.get("folder_16"));
		
		Label label_3 = new Label(shell, SWT.NONE);
		label_3.setText("监控间隔");
		
		spinner = new Spinner(shell, SWT.BORDER);
		spinner.setMaximum(1000);
		spinner.setMinimum(5);
		spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		spinner.setSelection(Integer.valueOf(AppConfigrator.getMonitorInterval()));
		
		Button button = new Button(shell, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startMonitor();
			}
		});
		button.setText("监控");
		button.setImage(ImageUtil.get("update_16"));
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
		
		Button btnNewButton_3 = new Button(composite, SWT.NONE);
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(1);
			}
		});
		btnNewButton_3.setText("退出");
		btnNewButton_3.setImage(ImageUtil.get("exit_16"));
		
		trayItem.setImage(image);
		trayItem.setToolTipText("自动监控拷贝");
		trayItem.setText("自动监控拷贝");
		
		ToolTip toolTip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
		trayItem.setToolTip(toolTip);
		toolTip.setText("提示");
		toolTip.setMessage("我在这里安安静静的....");
		
		listen();
	}
	
	protected void startMonitor() {
		AppConfigrator.setProperties(AppConfigrator.key_monitorInterval, String.valueOf(spinner.getSelection()));
		AppConfigrator.setProperties(AppConfigrator.key_monitorFolder, String.valueOf(text.getText()));
		AppConfigrator.setProperties(AppConfigrator.key_targetFolder, String.valueOf(text_1.getText()));
		
		MonitorService.getInstance().restart();
	}

	protected String selectFolder() {
		return new DirectoryDialog(shell).open();
	}

	public void listen(){
		trayItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				trayItem.setVisible(false);
				shell.setVisible(true);
			}
			
		});
		
		shell.addShellListener(new ShellAdapter() {

			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				shell.setVisible(false);
				trayItem.setVisible(true);
				
				shell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							TimeUnit.SECONDS.sleep(1);
							ToolTip toolTip = trayItem.getToolTip();
							toolTip.setAutoHide(true);
							toolTip.setVisible(true);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
				
			}
			
		});
	}
}
