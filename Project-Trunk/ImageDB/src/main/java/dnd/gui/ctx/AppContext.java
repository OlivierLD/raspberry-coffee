package dnd.gui.ctx;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class AppContext
		implements Serializable {
	private static final long serialVersionUID = 1L;
	private static AppContext instance = null;

	private transient Connection conn = null;
	private transient List<ImageAppListener> applicationListeners = null;
	private transient VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(this);
	private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	private AppContext()
	{
		this.applicationListeners = new ArrayList(2);
	}

	public static synchronized AppContext getInstance() {
		if (instance == null)
			instance = new AppContext();
		return instance;
	}

	public void setConn(Connection conn)
			throws PropertyVetoException {
		Connection oldConn = this.conn;
		this.vetoableChangeSupport.fireVetoableChange("Conn", oldConn, conn);
		this.conn = conn;
		this.propertyChangeSupport.firePropertyChange("Conn", oldConn, conn);
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

	public Connection getConn()
	{
		return this.conn;
	}

	public List<ImageAppListener> getListeners() {
		return this.applicationListeners;
	}

	public synchronized void addApplicationListener(ImageAppListener l) {
		if (!getListeners().contains(l)) {
			getListeners().add(l);
		}
	}

	public synchronized void removeApplicationListener(ImageAppListener l)
	{
		getListeners().remove(l);
	}

	public void closeConnection() {
		try {
			if (this.conn != null) {
				this.conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fireDisplayImage(String in) {
		getListeners().stream().forEach(ial -> ial.displayImage(in));
	}

	public void fireRefreshFromDB() {
		for (ImageAppListener ial : getListeners()) {
			ial.refreshFromDB();
		}
	}

	public void fireSetStatusLabel(String s) {
		for (ImageAppListener ial : getListeners()) {
			ial.setStatusLabel(s);
		}
	}

	public void fireActivateProgressBar(boolean b) {
		for (ImageAppListener ial : getListeners()) {
			ial.activateProgressBar(b);
		}
	}

	public void fireSetProgressBar(int v, int max) {
		for (ImageAppListener ial : getListeners()) {
			ial.setProgressBar(v, max);
		}
	}
}
