package dnd.gui.utils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;

public class ImageDefinition
		implements Serializable{
	private static final long serialVersionUID = 135560446016034697L;
	private String name;
	private String type;
	private int w;
	private int h;
	private String created;
	private String tags;
	private transient VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(this);
	private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public ImageDefinition(String name, String type, int w, int h, String created, String tags) {
		this.name = name;
		this.type = type;
		this.w = w;
		this.h = h;
		this.created = created;
		this.tags = tags;
	}

	public void setName(String name)
			throws PropertyVetoException {
		String oldName = this.name;
		this.vetoableChangeSupport.fireVetoableChange("Name", oldName, name);
		this.name = name;
		this.propertyChangeSupport.firePropertyChange("Name", oldName, name);
	}

	public void addVetoableChangeListener(VetoableChangeListener l) {
		this.vetoableChangeSupport.addVetoableChangeListener(l);
	}

	public void removeVetoableChangeListener(VetoableChangeListener l) {
		this.vetoableChangeSupport.removeVetoableChangeListener(l);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		this.propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		this.propertyChangeSupport.removePropertyChangeListener(l);
	}

	public String getName()
	{
		return this.name;
	}

	public void setType(String type)
			throws PropertyVetoException {
		String oldType = this.type;
		this.vetoableChangeSupport.fireVetoableChange("Type", oldType, type);
		this.type = type;
		this.propertyChangeSupport.firePropertyChange("Type", oldType, type);
	}

	public String getType()
	{
		return this.type;
	}

	public void setW(int w)
			throws PropertyVetoException {
		int oldW = this.w;
		this.vetoableChangeSupport.fireVetoableChange("W", oldW, w);
		this.w = w;
		this.propertyChangeSupport.firePropertyChange("W", oldW, w);
	}

	public int getW()
	{
		return this.w;
	}

	public void setH(int h)
			throws PropertyVetoException {
		int oldH = this.h;
		this.vetoableChangeSupport.fireVetoableChange("H", oldH, h);
		this.h = h;
		this.propertyChangeSupport.firePropertyChange("H", oldH, h);
	}

	public int getH()
	{
		return this.h;
	}

	public void setTags(String tags)
			throws PropertyVetoException {
		String oldTags = this.tags;
		this.vetoableChangeSupport.fireVetoableChange("Tags", oldTags, tags);
		this.tags = tags;
		this.propertyChangeSupport.firePropertyChange("Tags", oldTags, tags);
	}

	public String getTags()
	{
		return this.tags;
	}

	public void setCreated(String created)
			throws PropertyVetoException {
		String oldCreated = this.created;
		this.vetoableChangeSupport.fireVetoableChange("Created", oldCreated, created);
		this.created = created;
		this.propertyChangeSupport.firePropertyChange("Created", oldCreated, created);
	}

	public String getCreated()
	{
		return this.created;
	}
}
