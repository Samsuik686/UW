package com.jimi.agv.tracker.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.jimi.agv.tracker.task.controller.CushionController;
import com.jimi.agv.tracker.task.reporter.CushionHeadReporter;

/**
 * 缓冲任务
 * <br>
 * <b>2019年6月12日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class CushionAGVIOTask extends AGVIOTask{

	public CushionAGVIOTask(String taskFilePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(taskFilePath)));
		String item = null;
		while((item = reader.readLine()) != null) {
			String[] sourceTargetPair = item.split("\\s");
			String[] source = sourceTargetPair[0].split(",");
			String[] target = sourceTargetPair[1].split(",");
			getItems().add(new CushionAGVIOTaskItem(Integer.parseInt(source[0]), Integer.parseInt(source[1]), Integer.parseInt(source[2]), Integer.parseInt(target[0]), Integer.parseInt(target[1]), Integer.parseInt(target[2])));
		}
		reader.close();
		setReporter(new CushionHeadReporter(this));
		setController(new CushionController(this));
	}
	
}
