package org.processmining.framework.plugin;

import java.util.List;
import java.util.concurrent.Executor;


public interface PluginContext  {

	/**
	 * Returns a new plugin context instance, which can be used to invoke other
	 * plugins.
	 * 
	 * @return the new plugin context instance
	 */
	PluginContext createChildContext(String label);

	/* === Getters ==================================================== */

	/**
	 * Returns the progress object corresponding to this context
	 * 
	 * @return the progress object corresponding to this context
	 */
	Progress getProgress();

	/**
	 * Returns the list of registered progress listeners
	 * 
	 * @return the list of registered progress listeners
	 */
	
	/**
	 * Returns the label of this context.
	 * 
	 * @return
	 */
	String getLabel();

	

	/**
	 * Returns the context which created this context or null if it has no
	 * parent.
	 * 
	 * @return
	 */
	PluginContext getParentContext();

	/**
	 * Returns a list of all child contexts which have been created with
	 * createChildContext().
	 * 
	 * @return
	 */
	List<PluginContext> getChildContexts();

	

	/**
	 * Returns an executor which can be used to execute plugins in child
	 * contexts.
	 * 
	 * @return
	 */
	Executor getExecutor();

	/**
	 * Returns true if this is a distant child of context, i.e. true if
	 * getParent.getID().equals(context.getID()) ||
	 * getParent().isDistantChildOf(context);
	 * 
	 * @param context
	 * @return
	 */
	boolean isDistantChildOf(PluginContext context);

	
	/**
	 * Same as calling log(message, MessageLevel.NORMAL);
	 * 
	 * @param message
	 *            The message
	 */
	void log(String message);

	/**
	 * The provided Exception is provided to the context. It signals the context
	 * about an error in the plugin, that specifically lead to abnormal
	 * termination. The plugin signaling the exception is no longer executing!
	 * 
	 * @param exception
	 *            the exception thrown
	 */
	void log(Throwable exception);

	

	/**
	 * Returns the root plugin context. This is an instance of PluginContext of
	 * which all other contexts are distant childs.
	 * 
	 * @return
	 */
	PluginContext getRootContext();

	/**
	 * Delete this child from this context.
	 * 
	 * @param child
	 * @returns true if this child was a child of the context and has now been
	 *          deleted. false otherwise
	 */
	boolean deleteChild(PluginContext child);

	/**
	 * Registers the given connection in the global context. The implementation
	 * is
	 * 
	 * addConnection(this,c);
	 * 
	 * @param c
	 */
	

	void clear();
}
