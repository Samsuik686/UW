package com.jimi.uw_server.model.bo;

import com.jimi.uw_server.util.ExcelHelper.Excel;


/**
 * 
 * @author HardyYao
 * @createTime 2018年12月12日  下午2:11:01
 */

public class RegularMaterialTypeItemBO {

	@Excel(col = 0, head = "序号")
	private Integer serialNumber;

	@Excel(col = 1, head = "料号")
	private String no;

	@Excel(col = 2, head = "规格")
	private String specification;

	@Excel(col = 3, head = "厚度")
	private Integer thickness;

	@Excel(col = 4, head = "直径")
	private Integer radius;
	
    @Excel(col = 5, head = "是否超发")
    private String isSuperabled;

	public String getNo() {
		return no;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public String getSpecification() {
		return specification;
	}


	public void setSpecification(String specification) {
		this.specification = specification;
	}


	public Integer getThickness() {
		return thickness;
	}


	public void setThickness(Integer thickness) {
		this.thickness = thickness;
	}


	public Integer getRadius() {
		return radius;
	}


	public void setRadius(Integer radius) {
		this.radius = radius;
	}


	public Integer getSerialNumber() {
		return serialNumber;
	}


	public void setSerialNumber(Integer serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	
    public String getIsSuperabled() {
        return isSuperabled;
    }


    public void setIsSuperabled(String isSuperabled) {
        this.isSuperabled = isSuperabled;
    }

}
