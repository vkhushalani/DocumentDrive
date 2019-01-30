package com.ngahr.documentDrive.model;

import java.util.Date;

public class DocObject {
  private String id;
  private String name;
  private String size;
  private String createBy;
  private Date createdOn;
  private String type;
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getSize() {
	return size;
}
public void setSize(String size) {
	this.size = size;
}
public String getCreateBy() {
	return createBy;
}
public void setCreateBy(String createBy) {
	this.createBy = createBy;
}
public Date getCreatedOn() {
	return createdOn;
}
public void setCreatedOn(Date createdOn) {
	this.createdOn = createdOn;
}
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}
}
