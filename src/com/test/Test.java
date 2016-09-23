package com.test;

import com.project.Project;
import com.project.Project.TaskStatus;


/**
 * @author Boris
 * @description 
 * 2016Äê9ÔÂ21ÈÕ
 */
public class Test {
	public static void main(String[] args) {
		int i = 0;
		Project.getInstance().isDebug(true);
		while(i < 10){
			Project.getInstance().writeTaskMsg(i, "²âÊÔ" + i, 10, TaskStatus.DOING, 20 + i);
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			Project.getInstance().writeTaskMsg(i, "²âÊÔ" + i, 10, TaskStatus.DONE, 20 + i);
			i++;
		}
	}
}
