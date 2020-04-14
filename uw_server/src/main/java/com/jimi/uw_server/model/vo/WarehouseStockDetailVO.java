/**  
*  
*/  
package com.jimi.uw_server.model.vo;

/**  
 * <p>Title: WarehouseStockDetails</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年4月13日
 *
 */
public class WarehouseStockDetailVO {
	private int numberInStock;

    private int numberOutStock;

    private String operationType;

    /**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public WarehouseStockDetailVO() {
		this.numberInStock = 0;
		this.numberOutStock = 0;
		this.operationType = "";
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
    public void setOperationType(String operationType){
        this.operationType = operationType;
    }
    public String getOperationType(){
        return this.operationType;
    }
}
