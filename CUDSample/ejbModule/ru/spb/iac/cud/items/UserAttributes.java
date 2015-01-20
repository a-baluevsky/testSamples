package ru.spb.iac.cud.items;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "userAttributes", propOrder = {
    "uid",
    "attributes"
})
public class UserAttributes {
	
	@XmlElement(name = "uid" /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private String uid;
	
	@XmlElement(name = "attributes" /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private List<Attribute> attributes = new ArrayList<Attribute>() ;
  //  private List<String> roles;

    public UserAttributes() {
    }

    public String getUid() {
        return this.uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
    
    /*   public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }*/

    @Override
    public String toString() {
        return "{user " + attributes /*+ " " + roles + "}"*/;
    }
}
