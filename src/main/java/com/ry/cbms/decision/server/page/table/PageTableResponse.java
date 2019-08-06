package com.ry.cbms.decision.server.page.table;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 分页查询返回
 * 
 * @author maoyang
 *
 */
public class PageTableResponse implements Serializable {

	private static final long serialVersionUID = 620421858510718076L;

	private Integer recordsTotal;
	private Integer recordsFiltered;
	private Integer pageCount;
	private Collection<?> data;

	public PageTableResponse(Integer recordsTotal, Integer recordsFiltered,Integer pageCount, Collection<?> data) {
		super();
		this.recordsTotal = recordsTotal;
		this.recordsFiltered = recordsFiltered;
		this.pageCount=pageCount;
		this.data = data;
	}

	public Integer getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(Integer recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public Integer getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(Integer recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public Collection<?> getData() {
		return data;
	}

	public void setData(Collection<?> data) {
		this.data = data;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}
}