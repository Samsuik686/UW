/**  
*  
*/  
package com.jimi.uw_server.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jimi.uw_server.agv.dao.PositionTaskRobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.handle.PositionTaskHandler;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.BoxPositionTask;
import com.jimi.uw_server.model.BoxPositionTaskItem;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.bo.BoxPositonMoveTaskItemBO;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.model.vo.BoxPositionTaskItemVO;
import com.jimi.uw_server.model.vo.BoxPositionTaskVO;
import com.jimi.uw_server.util.ExcelHelper;

/**  
 * <p>Title: MaterialBoxMoveService</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月2日
 *
 */
public class MaterialBoxMoveService {

	private static final String GET_SEND_BOX_POSITION_TASK_ITEM_BY_TASK = "SELECT * FROM box_position_task_item WHERE bind_task_id = ? AND status = ? AND bind_id IS NOT NULL";
	
	private static final String GET_BACK_BOX_POSITION_TASK_ITEM_BY_TASK = "SELECT * FROM box_position_task_item WHERE bind_task_id = ? AND status = ? AND bind_id IS NULL";

	private static final String GET_BACK_BOX_POSITION_TASK_ITEM_BY_X2_Y2_Z2 = "SELECT * FROM box_position_task_item WHERE bind_task_id = ? AND status = ? AND x2 = ? AND y2 = ? AND z2 = ?";

	private static final String GET_MATERIAL_BOX_BY_X_Y_Z = "SELECT * FROM material_box WHERE enabled = 1 AND row = ? AND col = ? AND height = ?";

	public void createMoveTask(File file) {
		List<BoxPositonMoveTaskItemBO> taskItemBOs = null;
		try {
			ExcelHelper fileReader = ExcelHelper.from(file);
			 taskItemBOs = fileReader.unfill(BoxPositonMoveTaskItemBO.class, 0);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("任务创建失败，解析错误");
		}
		if (taskItemBOs != null && !taskItemBOs.isEmpty()) {
			List<BoxPositionTaskItem> boxPositionTaskItems = new ArrayList<BoxPositionTaskItem>(taskItemBOs.size());
			for (BoxPositonMoveTaskItemBO boxPositionMoveTaskItemBO : taskItemBOs) {
				if (boxPositionMoveTaskItemBO.getX1() != null && boxPositionMoveTaskItemBO.getX2() != null && boxPositionMoveTaskItemBO.getY1() != null && boxPositionMoveTaskItemBO.getY2() != null && boxPositionMoveTaskItemBO.getZ1() != null && boxPositionMoveTaskItemBO.getZ2() != null) {
					if (boxPositionMoveTaskItemBO.getX1() == 0 && boxPositionMoveTaskItemBO.getY1() == 0 && boxPositionMoveTaskItemBO.getZ1() == 0) {
						continue;
					}
					MaterialBox materialBox = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionMoveTaskItemBO.getX1(), boxPositionMoveTaskItemBO.getY1(), boxPositionMoveTaskItemBO.getZ1());
					if (materialBox == null || !materialBox.getIsOnShelf()) {
						throw new OperationException("坐标为[" + boxPositionMoveTaskItemBO.getX1() + "," + boxPositionMoveTaskItemBO.getY1() + "," + boxPositionMoveTaskItemBO.getZ1() + "]的料盒不存在或不在架");
					}
					BoxPositionTaskItem item = new BoxPositionTaskItem();
					item.setX1(boxPositionMoveTaskItemBO.getX1());
					item.setY1(boxPositionMoveTaskItemBO.getY1());
					item.setZ1(boxPositionMoveTaskItemBO.getZ1());
					item.setX2(boxPositionMoveTaskItemBO.getX2());
					item.setY2(boxPositionMoveTaskItemBO.getY2());
					item.setZ2(boxPositionMoveTaskItemBO.getZ2());
					item.setStatus(TaskItemState.WAIT_ASSIGN);
					boxPositionTaskItems.add(item);
				}
			}
			BoxPositionTask task = new BoxPositionTask();
			task.setName("移动料盒任务—" + new SimpleDateFormat("yyMMddhhmmss").format(new Date()));
			task.setState(TaskState.WAIT_START);
			task.save();
			BoxPositionTaskItem itemTemp = null;
			if (!boxPositionTaskItems.isEmpty()) {
				for (BoxPositionTaskItem item : boxPositionTaskItems) {
					item.setBindTaskId(task.getId());
					item.save();
				}
				for (BoxPositionTaskItem item : boxPositionTaskItems) {
					itemTemp = BoxPositionTaskItem.dao.findFirst(GET_BACK_BOX_POSITION_TASK_ITEM_BY_X2_Y2_Z2, task.getId(), TaskItemState.WAIT_ASSIGN, item.getX1(), item.getY1(), item.getZ1());
					if (itemTemp != null) {
						item.setBindId(itemTemp.getId()).update();
					}
				}
			}
		}
		
		
	}
	
	
	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 */
	public void start(Integer taskId) {
		
		BoxPositionTask task = BoxPositionTask.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("查无此任务");
		}
		BoxPositionTask taskTemp = BoxPositionTask.dao.findFirst("SELECT * FROM box_position_task WHERE state = ?", TaskState.PROCESSING);
		if (PositionTaskRobotInfoRedisDAO.getPositionTaskStatus() || taskTemp != null) {
			throw new OperationException("已存在开始的任务，同一时间仅能开始一个任务");
		}
		task.setState(TaskState.PROCESSING).update();
		PositionTaskRobotInfoRedisDAO.setPositionTaskStatus(true);
		Thread thread = new Thread(()->{
			List<RobotBO> freeRobots = new ArrayList<>();
			List<RobotBO> robotBOs = RobotInfoRedisDAO.check();
			if (robotBOs != null) {
				for (RobotBO robot : robotBOs) {
					// 筛选空闲或充电状态的处于启用中的叉车
					if ((robot.getStatus() == 0 || robot.getStatus() == 4) && robot.getEnabled() == 2) {
						freeRobots.add(robot);
					}
				}
			}
			if (!freeRobots.isEmpty()) {
				for (RobotBO robotBO : freeRobots) {
					if (PositionTaskRobotInfoRedisDAO.getRobotStatus(robotBO.getId())) {
						continue;
					}
					//发送取的指令
					BoxPositionTaskItem boxPositionTaskItem = BoxPositionTaskItem.dao.findFirst(GET_SEND_BOX_POSITION_TASK_ITEM_BY_TASK, task.getId(), TaskItemState.WAIT_ASSIGN);
					if (boxPositionTaskItem != null) {
						try {
							MaterialBox materialBox = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX1(), boxPositionTaskItem.getY1(), boxPositionTaskItem.getZ1());
							MaterialBox materialBox2 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(),boxPositionTaskItem.getZ2());
							if (materialBox2 == null) {
								PositionTaskHandler.getInstance().sendLL(boxPositionTaskItem.getId() , materialBox, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(), boxPositionTaskItem.getZ2(), robotBO.getId());
								boxPositionTaskItem.setRobotId(robotBO.getId()).setStatus(TaskItemState.ASSIGNED).update();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else {
					//发送回的指令
					boxPositionTaskItem = BoxPositionTaskItem.dao.findFirst(GET_BACK_BOX_POSITION_TASK_ITEM_BY_TASK, task.getId(), TaskItemState.WAIT_ASSIGN);
						if (boxPositionTaskItem != null) {
							try {
								MaterialBox materialBox = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX1(), boxPositionTaskItem.getY1(), boxPositionTaskItem.getZ1());
								MaterialBox materialBox2 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(),boxPositionTaskItem.getZ2());
								if (materialBox2 == null) {
									PositionTaskHandler.getInstance().sendLL(boxPositionTaskItem.getId() , materialBox, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(), boxPositionTaskItem.getZ2(), robotBO.getId());
									boxPositionTaskItem.setRobotId(robotBO.getId()).setStatus(TaskItemState.ASSIGNED).update();
								}
							} catch (Exception e) {
								
							}
						}else {
							return;
						}
					}
					
				}
			}
		});
		thread.setName("PositionTaskThread");
		thread.start();
	}
	
	
	public void switchTask(Integer taskId, Boolean flag) {
		
		BoxPositionTask task = BoxPositionTask.dao.findById(taskId);
		if (task == null || !task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("查无此任务，或者任务未处于进行中状态");
		}
		if (flag == true) {
			if (PositionTaskRobotInfoRedisDAO.getPositionTaskStatus()) {
				throw new OperationException("已存在开始的任务，同一时间仅能开始一个任务");
			}
		}
		PositionTaskRobotInfoRedisDAO.setPositionTaskStatus(flag);
		if (flag == true) {
			Thread thread = new Thread(()->{
				List<RobotBO> freeRobots = new ArrayList<>();
				List<RobotBO> robotBOs = RobotInfoRedisDAO.check();
				if (robotBOs != null) {
					for (RobotBO robot : robotBOs) {
						// 筛选空闲或充电状态的处于启用中的叉车
						if ((robot.getStatus() == 0 || robot.getStatus() == 4) && robot.getEnabled() == 2) {
							freeRobots.add(robot);
						}
					}
				}
				if (!freeRobots.isEmpty()) {
					for (RobotBO robotBO : freeRobots) {
						if (PositionTaskRobotInfoRedisDAO.getRobotStatus(robotBO.getId())) {
							continue;
						}
						//发送取的指令
						BoxPositionTaskItem boxPositionTaskItem = BoxPositionTaskItem.dao.findFirst(GET_SEND_BOX_POSITION_TASK_ITEM_BY_TASK, task.getId(), TaskItemState.WAIT_ASSIGN);
						if (boxPositionTaskItem != null) {
							try {
								MaterialBox materialBox = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX1(), boxPositionTaskItem.getY1(), boxPositionTaskItem.getZ1());
								MaterialBox materialBox2 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(),boxPositionTaskItem.getZ2());
								if (materialBox2 == null) {
									PositionTaskHandler.getInstance().sendLL(boxPositionTaskItem.getId() , materialBox, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(), boxPositionTaskItem.getZ2(), robotBO.getId());
									boxPositionTaskItem.setRobotId(robotBO.getId()).setStatus(TaskItemState.ASSIGNED).update();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else {
						//发送回的指令
						boxPositionTaskItem = BoxPositionTaskItem.dao.findFirst(GET_BACK_BOX_POSITION_TASK_ITEM_BY_TASK, task.getId(), TaskItemState.WAIT_ASSIGN);
							if (boxPositionTaskItem != null) {
								try {
									MaterialBox materialBox = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX1(), boxPositionTaskItem.getY1(), boxPositionTaskItem.getZ1());
									MaterialBox materialBox2 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(),boxPositionTaskItem.getZ2());
									if (materialBox2 == null) {
										PositionTaskHandler.getInstance().sendLL(boxPositionTaskItem.getId() , materialBox, boxPositionTaskItem.getX2(), boxPositionTaskItem.getY2(), boxPositionTaskItem.getZ2(), robotBO.getId());
										boxPositionTaskItem.setRobotId(robotBO.getId()).setStatus(TaskItemState.ASSIGNED).update();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}else {
								return;
							}
						}
						
					}
				}
			});
			thread.setName("PositionTaskThread");
			thread.start();
		}
		
	}
	
	public void cancelTask(Integer taskId) {
		BoxPositionTask task = BoxPositionTask.dao.findById(taskId);
		if (task == null || !task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("查无此任务，或者任务未处于进行中状态");
		}
		PositionTaskRobotInfoRedisDAO.setPositionTaskStatus(false);
		task.setState(TaskState.CANCELED).update();
	}
	
	
	public List<BoxPositionTaskVO> listBoxPositionTask(){
		List<BoxPositionTask> tasks = BoxPositionTask.dao.find("SELECT * FROM box_position_task ORDER BY id DESC");
		return BoxPositionTaskVO.fillList(tasks);
	}
	
	
	public List<BoxPositionTaskItemVO> listBoxPositionTaskItem(Integer taskId){
	  BoxPositionTask task = BoxPositionTask.dao.findById(taskId); 
	  if (task ==null) { 
		 throw new OperationException("查无此任务"); 
	  }
	  List<BoxPositionTaskItem> items = BoxPositionTaskItem.dao.find("SELECT * FROM box_position_task_item WHERE bind_task_id = ? ORDER BY id ASC", task.getId());
	  return BoxPositionTaskItemVO.fillList(items);
	  
	}
	 
}
