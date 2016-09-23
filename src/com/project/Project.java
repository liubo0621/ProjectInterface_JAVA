package com.project;

import com.listener.CommandListener;
import com.utils.Tools;

/**
 * @author Boris
 * @description 
 * 2016年9月21日
 */

public class Project implements Runnable{
	public static enum TaskStatus{
		DOING(0x0000001),
		DONE(0x0000002),
		EXCEPTION(0x0000003),
		OTHER(0x0000000);
		
		private final int value;
		
		TaskStatus(int value){
			this.value = value;
		}
		
		public int getValue(){
			return value;
		}
	}
	
	private Tools tools;
	private CommandListener commandListener;
	
	private String projectNamme;
	private String statusFile;
	private String commandFile;
	
	private int readCommandTime;
	private int processPid;
	private int threadNum;
	private int allowedMaxThreadNum;
	private int taskDoneNum;
	
	private boolean isDebug;
	
	private Project(){
		tools =  Tools.getTools();
		projectNamme = tools.getProperty("project_name");
		statusFile = tools.getProperty("project.status_file");
		commandFile = tools.getProperty("client.command_file");
		readCommandTime = Integer.parseInt(tools.getProperty("project.read_file_time")) * 1000;
		processPid = tools.getPid();
		threadNum = 0;
		taskDoneNum = 0;
		allowedMaxThreadNum =0xfffffff;
		isDebug = false;
		
		new Thread(this).start();
	}

	//instance
	private static class ProjectHolder{
		public static final Project project = new Project();
	}
	
	public static Project getInstance(){
		return ProjectHolder.project;
	}
	
	//method
	public void isDebug(boolean isDebug){
		this.isDebug = isDebug;
	}
	
	
	/**
	 * @Method: threadStarted 
	 * @Description:线程开启时调用 用于统计线程数
	 * boolean  返回true 则可开启线程  false不可开启线程
	 */
	public synchronized boolean threadStarted(){
		if (threadNum <= allowedMaxThreadNum || allowedMaxThreadNum <= 0) {
			threadNum++;
			return true;
		}
		return false;
	}
	
	/**
	 * @Method: threadClosed 
	 * @Description: 线程结束时调用  用于统计线程数
	 * void
	 */
	public synchronized void threadClosed(){
		threadNum--;
	}
	
	
	/**
	 * @Method: exception 
	 * @Description: 程序发异常时调用（非任务引起的异常）
	 * @param e 异常
	 * void
	 */
	public void exception(Exception e){
		String exceptionMsg = e.toString();
		exception(exceptionMsg);
	}
	
	/**
	 * @Method: exception 
	 * @Description: 程序发异常时调用（非任务引起的异常）
	 * @param exceptionMsg 异常信息
	 * void
	 */
	public void exception(String exceptionMsg){
		boolean isException = true;
		int taskId = 0;
		String taskName = null;
		int taskLength = 0;
		TaskStatus taskStatus = TaskStatus.OTHER;
		int taskDoneNum = this.taskDoneNum;
		write(isException, taskId, taskName, taskLength, taskStatus, taskDoneNum, exceptionMsg);
	}
	
	/**
	 * @Method: writeTaskMsg 
	 * @Description: 写任务信息
	 * @param taskId 任务id
	 * @param taskName 任务名
	 * @param taskLength 任务时长 单位分钟
	 * @param taskStatus 任务状态 TaskStatus.DONE || TaskStatus.DOING || TaskStatus.EXCEPTION 
	 * @param taskDoneNum 已做完任务数
	 * @param exceptionMsg 异常信息
	 * void
	 */
	public void writeTaskMsg(int taskId, String taskName, int taskLength, TaskStatus taskStatus, int taskDoneNum){
		boolean isException = false;
		this.taskDoneNum = taskDoneNum;
		
		write(isException, taskId, taskName, taskLength, taskStatus, taskDoneNum, null);
	}
	
	/**
	 * @Method: writeTaskMsg 
	 * @Description: 写任务信息
	 * @param taskId 任务id
	 * @param taskName 任务名
	 * @param taskLength 任务时长 单位分钟
	 * @param taskStatus 任务状态 TaskStatus.DONE || TaskStatus.DOING || TaskStatus.EXCEPTION 
	 * @param taskDoneNum 已做完任务数
	 * @param e 异常
	 * void
	 */
	public void writeTaskMsg(int taskId, String taskName, int taskLength, TaskStatus taskStatus, int taskDoneNum, Exception e){
		String exceptionMsg = e == null ? null : e.toString();
		boolean isException = false;
		this.taskDoneNum = taskDoneNum;
		
		write(isException, taskId, taskName, taskLength, taskStatus, taskDoneNum, exceptionMsg);
	}
	
	//<process_id = 6073,write_file_time=2016-09-08 12:32:64,process_name=xxx,exception=true,thread_id=1,thread_num=10,task_id=5,task_name=xxx,task_length=12,task_status=2,task_done_num=20,exception_msg=xxx/>
	private synchronized void write(boolean isException, int taskId, String taskName, int taskLength, TaskStatus taskStatus, int taskDoneNum, String exceptionMsg){
		String time = tools.getCurrentTime();
		long threadId = tools.getThreadId();
		
		String msg = String.format("<process_id=%d,write_file_time=%s,process_name=%s,exception=%b,thread_id=%d,thread_num=%d,task_id=%d,task_name=%s,task_length=%d,task_status=%d,task_done_num=%d,exception_msg=%s/>",
							processPid, time, projectNamme, isException, threadId, threadNum, taskId, taskName, taskLength, taskStatus.value, taskDoneNum, exceptionMsg);
		if (isDebug) {
			System.out.println("write: " + msg);
		}

		tools.writeFile(statusFile, msg+ "\r\n");
	}
	
	private void readCommand(){
		while(true){
			String command = tools.readFile(commandFile);
			if(command.length() > 0){
				dealCommand(command);
			}
			try {
				Thread.sleep(readCommandTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	TASK:STOP taskId,threadId
//	THRead:MAX:NUM threadNum
	private void dealCommand(String command){
		if (isDebug) {
			System.out.println("receive command: " + command);
		}
		
		String commandHead = command.split(" ")[0];
		String commandContent = command.split(" ")[1];
		if (commandHead.equals("TASK:STOP")) {
			int taskId = Integer.parseInt(commandContent.split(",")[0]);
			int threadId = Integer.parseInt(commandContent.split(",")[1]);
			
			if(commandListener != null){
				commandListener.stopTask(taskId, threadId);
			}
			
		}else if(commandHead.startsWith("THR")){
			 allowedMaxThreadNum = Integer.parseInt(commandContent);
		}
	}
	
	/**
	 * @Method: setCommandListener
	 * @Description: 在应用程序入口处设置命令监听事件
	 * @param commandListener
	 * void
	 */
	public void setCommandListener(CommandListener commandListener){
		this.commandListener = commandListener;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		readCommand();
	}
}
