package org.pan;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorService{
	private Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);
	private static MonitorService monitorService;
	private ScheduledExecutorService scheduledService;
	private FileFilter fileFilter;
	private MonitorService(){}
	
	public static MonitorService getInstance(){
		if(monitorService == null){
			monitorService = new MonitorService();
		}
		return monitorService;
	}
	
	public FileFilter getFileFilter(){
		if(this.fileFilter == null){
			this.fileFilter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().endsWith(".zip");
				}
			};
		}
		return fileFilter;
	}
	
	public void restart(){
		if(scheduledService != null){
			LOGGER.info("monitorService has been shutdown");
			scheduledService.shutdown();
		}
		LOGGER.info("monitorService start now");
		scheduledService = Executors.newSingleThreadScheduledExecutor();
		Integer interval = Integer.valueOf(AppConfigrator.getMonitorInterval());
		scheduledService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try{
					LOGGER.debug("monitorService start check synchronize");
					String monitorFolderPath = AppConfigrator.getMonitorFolder();
					String targetFolderPath = AppConfigrator.getTargetFolder();
					
					
					File[] listFiles = new File(monitorFolderPath).listFiles(getFileFilter());
					if(listFiles == null || listFiles.length == 0){
						LOGGER.debug("there is nothing file changed,but next will continue");
						return;
					}
					for (File file : listFiles) {
						File file2 = new File(targetFolderPath + File.separator + file.getName());
						LOGGER.debug("monitorService start check file:{}",file.getAbsolutePath());
						if(file2.exists()){
							continue;
						}
						//在这里第隔5秒检查一下文件的大小，如果文件在变，则 说明文件还在写状态，暂不要复制
						Path monitorFilePath = Paths.get(monitorFolderPath, file.getName());
						long lastSize = Files.size(monitorFilePath);
						while(true){
							TimeUnit.SECONDS.sleep(5);
							long nowSize = Files.size(monitorFilePath);
							if(lastSize == nowSize){
								break;
							}
							lastSize = nowSize;
							LOGGER.debug("monitorService check file is Changing,next time will be check again :{}",file.getAbsolutePath());
						}
						
						Path path = Paths.get(targetFolderPath, file.getName());
						LOGGER.debug("monitorService start copy file:{} to: {}",file.getAbsolutePath(),path.toUri().toString());
						try(FileInputStream fis = new FileInputStream(file);){
							Files.copy(fis, path, StandardCopyOption.REPLACE_EXISTING);
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
				}catch(Exception e){
					LOGGER.error("monitorService has error",e);
				}
			}
		}, 3, interval, TimeUnit.SECONDS);
	}

}
