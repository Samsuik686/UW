/**  
*  
*/  
package com.jimi.uw_server.model.vo;

import java.util.List;

/**  
 * <p>Title: WarehouseStockVO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年4月13日
 *
 */
public class WarehouseStockVO {
	private String warehouse;

    private int oldBalance;

    private int currentBalance;

    private int numberInStock;

    private int numberOutStock;

    private List<WarehouseStockDetailVO> details;

    public void setWarehouse(String warehouse){
        this.warehouse = warehouse;
    }
    public String getWarehouse(){
        return this.warehouse;
    }
    public void setOldBalance(int oldBalance){
        this.oldBalance = oldBalance;
    }
    public int getOldBalance(){
        return this.oldBalance;
    }
    public void setCurrentBalance(int currentBalance){
        this.currentBalance = currentBalance;
    }
    public int getCurrentBalance(){
        return this.currentBalance;
    }
    public void setNumberInStock(int numberInStock){
        this.numberInStock = numberInStock;
    }
    public int getNumberInStock(){
        return this.numberInStock;
    }
    public void setNumberOutStock(int numberOutStock){
        this.numberOutStock = numberOutStock;
    }
    public int getNumberOutStock(){
        return this.numberOutStock;
    }
    public void setDetails(List<WarehouseStockDetailVO> details){
        this.details = details;
    }
    public List<WarehouseStockDetailVO> getDetails(){
        return this.details;
    }
}
