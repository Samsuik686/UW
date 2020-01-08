/**  
*  
*/  
package com.jimi.uw_server.agv.handle;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.PositionTaskRobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVMissionGroup;
import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.handle.base.BaseTaskHandler;
import com.jimi.uw_server.agv.socket.AGVMainSocket;
import com.jimi.uw_server.constant.PositionTaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.model.BoxPositionTask;
import com.jimi.uw_server.model.BoxPositionTaskItem;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialBox;

/**  
 * <p>Title: PositionTaskHandler</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月3日
 *
 */
public class PositionTaskHandler extends BaseTaskHandler {

	private volatile static PositionTaskHandler me;
	
	private static final Object LITTLE_LOCK = new Object();
	
	private static final String GET_MATERIAL_BOX_BY_X_Y_Z = "SELECT * FROM material_box WHERE enabled = 1 AND row = ? AND col = ? AND height = ?";
	
	private static final String GET_START_BOX_POSITION_TASK_ITEM_BY_TASK_AND_STATUS = "SELECT * FROM box_position_task_item WHERE bind_task_id = ? AND status = ? AND bind_id IS NOT NULL";
	
	private static final String GET_BACK_BOX_POSITION_TASK_ITEM_BY_TASK_AND_STATUS = "SELECT * FROM box_position_task_item WHERE bind_task_id = ? AND status = ? AND bind_id IS NULL";

	private static final String GET_BOX_POSITION_TASK_ITEM_BY_TASK_AND_NON_STATUS = "SELECT * FROM box_position_task_item WHERE bind_task_id = ? AND status != ? ";

	private PositionTaskHandler() {
	}


	public static PositionTaskHandler getInstance() {
		if (me == null) {
			synchronized (PositionTaskHandler.class) {
				if (me == null) {
					me = new PositionTaskHandler();
				}
			}
		}
		return me;
	}
	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 * @param item
	 * @param materialBox
	 * @param goodsLocation
	 * @param priority
	 * @throws Exception  
	 * @see com.jimi.uw_server.agv.handle.base.BaseTaskHandler#sendSendLL(com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem, com.jimi.uw_server.model.MaterialBox, com.jimi.uw_server.model.GoodsLocation, java.lang.Integer)  
	 */
	@Override
	public void sendSendLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority)
			throws Exception {

	}
	
	
	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 * @param item
	 * @param materialBox
	 * @param goodsLocation
	 * @param priority
	 * @throws Exception  
	 * @see com.jimi.uw_server.agv.handle.base.BaseTaskHandler#sendBackLL(com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem, com.jimi.uw_server.model.MaterialBox, com.jimi.uw_server.model.GoodsLocation, java.lang.Integer)  
	 */
	@Override
	public void sendBackLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority)
			throws Exception {
		// TODO Auto-generated method stub

	}
	
	
	public void sendLL(Integer id, MaterialBox materialBox, Integer x2, Integer y2, Integer z2, Integer robotId) throws Exception {
		AGVMoveCmd cmd = createSendLLCmd(id, materialBox.getRow(), materialBox.getCol(), materialBox.getHeight(), x2, y2, z2, robotId);
		AGVMainSocket.sendMessage(Json.getJson().toJson(cmd));
		materialBox.setIsOnShelf(false).update();
		PositionTaskRobotInfoRedisDAO.setRobotStatus(robotId, true);
		Thread.sleep(300);
	}


	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 * @param statusCmd
	 * @throws Exception  
	 * @see com.jimi.uw_server.agv.handle.base.BaseTaskHandler#handleStartStatus(com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd)  
	 */
	@Override
	protected void handleStartStatus(AGVStatusCmd statusCmd) throws Exception {
		// TODO Auto-generated method stub
		String groupId = statusCmd.getMissiongroupid();
		String[] groupIds = groupId.split("_");
		Integer id = Integer.valueOf(groupIds[1]);
		BoxPositionTaskItem item = BoxPositionTaskItem.dao.findById(id);
		item.setStatus(PositionTaskItemState.ACCEPT).update();
	}

	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 * @param statusCmd
	 * @throws Exception  
	 * @see com.jimi.uw_server.agv.handle.base.BaseTaskHandler#handleProcessStatus(com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd)  
	 */
	@Override
	protected void handleProcessStatus(AGVStatusCmd statusCmd) throws Exception {
		// TODO Auto-generated method stub
		String groupId = statusCmd.getMissiongroupid();
		String[] groupIds = groupId.split("_");
		Integer id = Integer.valueOf(groupIds[1]);
		BoxPositionTaskItem item = BoxPositionTaskItem.dao.findById(id);
		item.setStatus(PositionTaskItemState.CATCH).update();
	}

	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 * @param statusCmd
	 * @throws Exception  
	 * @see com.jimi.uw_server.agv.handle.base.BaseTaskHandler#handleFinishStatus(com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd)  
	 */
	@Override
	protected void handleFinishStatus(AGVStatusCmd statusCmd) throws Exception {
		// TODO Auto-generated method stub
		String groupId = statusCmd.getMissiongroupid();
		String[] groupIds = groupId.split("_");
		Integer id = Integer.valueOf(groupIds[1]);
		BoxPositionTaskItem item = BoxPositionTaskItem.dao.findById(id);
		if (item == null) {
			System.err.println(Json.getJson().toJson(statusCmd) + "【任务条目为空】");
		}
		item.setStatus(PositionTaskItemState.FINISH).update();
		PositionTaskRobotInfoRedisDAO.setRobotStatus(item.getRobotId(), false);
		MaterialBox materialBox = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, item.getX1(), item.getY1(), item.getZ1());
		if (materialBox != null) {
			materialBox.setRow(item.getX2()).setCol(item.getY2()).setHeight(item.getZ2()).setIsOnShelf(true).update();
		}else {
			System.err.println(Json.getJson().toJson(statusCmd) + "【料盒为空】");
		}
		Thread thread = new Thread(()->{
			try {
				BoxPositionTask task = BoxPositionTask.dao.findById(item.getBindTaskId());
				synchronized (LITTLE_LOCK) {
					if (PositionTaskRobotInfoRedisDAO.getPositionTaskStatus() && task.getState().equals(TaskState.PROCESSING)) {
						if (item.getBindId() != null) {
							BoxPositionTaskItem item2 = BoxPositionTaskItem.dao.findById(item.getBindId());
							if (item2 != null && item2.getStatus().equals(PositionTaskItemState.WAIT)) {
								MaterialBox materialBox2 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, item2.getX1(), item2.getY1(), item2.getZ1());
								if (materialBox2 != null) {
									PositionTaskHandler.getInstance().sendLL(item2.getId(), materialBox2, item2.getX2(), item2.getY2(), item2.getZ2(), item.getRobotId());
									item2.setRobotId(item.getRobotId()).setStatus(PositionTaskItemState.ASSIGN).update();
									return ;								
								}else {
									System.err.println(Json.getJson().toJson(item2) + "【料盒为空】");
								}
								
							}
						}
						List<BoxPositionTaskItem> items = BoxPositionTaskItem.dao.find(GET_START_BOX_POSITION_TASK_ITEM_BY_TASK_AND_STATUS, item.getBindTaskId(), PositionTaskItemState.WAIT);
						if (items != null && !items.isEmpty()) {
							for (BoxPositionTaskItem item2 : items) {
								MaterialBox materialBox2 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, item2.getX2(), item2.getY2(), item2.getZ2());
								if (materialBox2 != null) {
									continue;
								}
								MaterialBox materialBox3 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, item2.getX1(), item2.getY1(), item2.getZ1());
								if (materialBox3 != null) {
									PositionTaskHandler.getInstance().sendLL(item2.getId(), materialBox3, item2.getX2(), item2.getY2(), item2.getZ2(), item.getRobotId());
									item2.setRobotId(item.getRobotId()).setStatus(PositionTaskItemState.ASSIGN).update();
									return;
								}
								
							}
						}
						items = BoxPositionTaskItem.dao.find(GET_BACK_BOX_POSITION_TASK_ITEM_BY_TASK_AND_STATUS, item.getBindTaskId(), PositionTaskItemState.WAIT);
						if (items != null && !items.isEmpty()) {
							for (BoxPositionTaskItem item2 : items) {
								MaterialBox materialBox2 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, item2.getX2(), item2.getY2(), item2.getZ2());
								if (materialBox2 != null) {
									continue;
								}
								MaterialBox materialBox3 = MaterialBox.dao.findFirst(GET_MATERIAL_BOX_BY_X_Y_Z, item2.getX1(), item2.getY1(), item2.getZ1());
								if (materialBox3 != null) {
									PositionTaskHandler.getInstance().sendLL(item2.getId(), materialBox3, item2.getX2(), item2.getY2(), item2.getZ2(), item.getRobotId());
									item2.setRobotId(item.getRobotId()).setStatus(PositionTaskItemState.ASSIGN).update();
									return;
								}
							}
						}
						
						BoxPositionTaskItem boxPositionTaskItem = BoxPositionTaskItem.dao.findFirst(GET_BOX_POSITION_TASK_ITEM_BY_TASK_AND_NON_STATUS, item.getBindTaskId(), PositionTaskItemState.FINISH);
						if (boxPositionTaskItem == null) {
							task.setState(TaskState.FINISHED).update();
							PositionTaskRobotInfoRedisDAO.delPositionTaskStatus();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		});
		thread.setName("PositionHandlerFinishThread");
		thread.start();
		
	}


	public AGVMoveCmd createSendLLCmd(Integer id, Integer x1, Integer y1, Integer z1, Integer x2, Integer y2, Integer z2, Integer robotId) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid("pt_" + id);
		group.setRobotid(0);
		group.setStartx(x1);// 设置X
		group.setStarty(y1);// 设置Y
		group.setStartz(z1);// 设置Z
		group.setEndx(x2);// 终点X为货位X
		group.setEndy(y2);// 终点Y为货位Y
		group.setEndz(z2);// 终点Z为货位Z
		group.setPriority(String.valueOf(0));
		group.setRobotid(robotId);
		groups.add(group);
		AGVMoveCmd moveCmd = new AGVMoveCmd();
		moveCmd.setCmdcode("LL");
		moveCmd.setCmdid(TaskItemRedisDAO.getCmdId());
		moveCmd.setMissiongroups(groups);
		return moveCmd;
	}

}
