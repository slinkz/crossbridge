/* JMenu.java --
   Copyright (C) 2002, 2004, 2005, 2006  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package javax.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.MenuItemUI;

/**
 * This class represents a menu that can be added to a menu bar or
 * can be a submenu in some other menu. When JMenu is selected it
 * displays JPopupMenu containing its menu items.
 *
 * <p>
 * JMenu's fires MenuEvents when this menu's selection changes. If this menu
 * is selected, then fireMenuSelectedEvent() is invoked. In case when menu is
 * deselected or cancelled, then fireMenuDeselectedEvent() or 
 * fireMenuCancelledEvent() is invoked, respectivelly.
 * </p>
 */
public class JMenu extends JMenuItem implements Accessible, MenuElement
{
  private static final long serialVersionUID = 4227225638931828014L;

  /** A Popup menu associated with this menu, which pops up when menu is selected */
  private JPopupMenu popupMenu = null;

  /** Whenever menu is selected or deselected the MenuEvent is fired to
     menu's registered listeners. */
  private MenuEvent menuEvent = new MenuEvent(this);

  /*Amount of time, in milliseconds, that should pass before popupMenu
    associated with this menu appears or disappers */
  private int delay;

  /* PopupListener */
  protected WinListener popupListener;

  /** Location at which popup menu associated with this menu will be
     displayed */
  private Point menuLocation;

  /**
   * Creates a new JMenu object.
   */
  public JMenu()
  {
    super();
    setOpaque(false);
    setDelay(200);
  }

  /**
   * Creates a new <code>JMenu</code> with the specified label.
   *
   * @param text label for this menu
   */
  public JMenu(String text)
  {
    super(text);
    popupMenu = new JPopupMenu(); 
    popupMenu.setInvoker(this);
    setOpaque(false);
    setDelay(200);
  }

  /**
   * Creates a new <code>JMenu</code> object.
   *
   * @param action Action that is used to create menu item tha will be
   * added to the menu.
   */
  public JMenu(Action action)
  {
    super(action);
    createActionChangeListener(this);
    popupMenu = new JPopupMenu();
    popupMenu.setInvoker(this);
    setOpaque(false);
    setDelay(200);
  }

  /**
   * Creates a new <code>JMenu</code> with specified label and an option
   * for this menu to be tear-off menu.
   *
   * @param text label for this menu
   * @param tearoff true if this menu should be tear-off and false otherwise
   */
  public JMenu(String text, boolean tearoff)
  {
    // FIXME: tearoff not implemented
    this(text);
    setDelay(200);
  }

  /**
   * Adds specified menu item to this menu
   *
   * @param item Menu item to add to this menu
   *
   * @return Menu item that was added
   */
  public JMenuItem add(JMenuItem item)
  {
    return getPopupMenu().add(item);
  }

  /**
   * Adds specified component to this menu.
   *
   * @param component Component to add to this menu
   *
   * @return Component that was added
   */
  public Component add(Component component)
  {
    getPopupMenu().insert(component, -1);
    return component;
  }

  /**
   * Adds specified component to this menu at the given index
   *
   * @param component Component to add
   * @param index Position of this menu item in the menu
   *
   * @return Component that was added
   */
  public Component add(Component component, int index)
  {
    return getPopupMenu().add(component, index);
  }

  /**
   * Adds JMenuItem constructed with the specified label to this menu
   *
   * @param text label for the menu item that will be added
   *
   * @return Menu Item that was added to this menu
   */
  public JMenuItem add(String text)
  {
    return getPopupMenu().add(text);
  }

  /**
   * Adds JMenuItem constructed using properties from specified action.
   *
   * @param action action to construct the menu item with
   *
   * @return Menu Item that was added to this menu
   */
  public JMenuItem add(Action action)
  {
    return getPopupMenu().add(action);
  }

  /**
   * Removes given menu item from this menu. Nothing happens if
   * this menu doesn't contain specified menu item.
   *
   * @param item Menu Item which needs to be removed
   */
  public void remove(JMenuItem item)
  {
    getPopupMenu().remove(item);
  }

  /**
   * Removes component at the specified index from this menu
   *
   * @param index Position of the component that needs to be removed in the menu
   */
  public void remove(int index)
  {
    if (index < 0 || (index > 0 && getMenuComponentCount() == 0))
      throw new IllegalArgumentException();
  
    if (getMenuComponentCount() > 0)
      popupMenu.remove(index);
  }

  /**
   * Removes given component from this menu.
   *
   * @param component Component to remove
   */
  public void remove(Component component)
  {
    int index = getPopupMenu().getComponentIndex(component);
    if (index >= 0)
      getPopupMenu().remove(index);
  }

  /**
   * Removes all menu items from the menu
   */
  public void removeAll()
  {
    if (popupMenu != null)
      popupMenu.removeAll();
  }

  /**
   * Creates JMenuItem with the specified text and inserts it in the
   * at the specified index
   *
   * @param text label for the new menu item
   * @param index index at which to insert newly created menu item.
   */
  public void insert(String text, int index)
  {
    this.insert(new JMenuItem(text), index);
  }

  /**
   * Creates JMenuItem with the specified text and inserts it in the
   * at the specified index. IllegalArgumentException is thrown
   * if index is less than 0
   *
   * @param item menu item to insert
   * @param index index at which to insert menu item.
   * @return Menu item that was added to the menu
   */
  public JMenuItem insert(JMenuItem item, int index)
  {
    if (index < 0)
      throw new IllegalArgumentException("index less than zero");

    getPopupMenu().insert(item, index);
    return item;
  }

  /**
   * Creates JMenuItem with the associated action and inserts it to the menu
   * at the specified index. IllegalArgumentException is thrown
   * if index is less than 0
   *
   * @param action Action for the new menu item
   * @param index index at which to insert newly created menu item.
   * @return Menu item that was added to the menu
   */
  public JMenuItem insert(Action action, int index)
  {
    JMenuItem item = new JMenuItem(action);
    this.insert(item, index);

    return item;
  }

  /**
   * This method sets this menuItem's UI to the UIManager's default for the
   * current look and feel.
   */
  public void updateUI()
  {
    setUI((MenuItemUI) UIManager.getUI(this));
  }

  /**
   * This method returns a name to identify which look and feel class will be
   * the UI delegate for the menu.
   *
   * @return The Look and Feel classID. "MenuUI"
   */
  public String getUIClassID()
  {
    return "MenuUI";
  }

  /**
   * Sets model for this menu.
   *
   * @param model model to set
   */
  public void setModel(ButtonModel model)
  {
    super.setModel(model);
  }

  /**
   * Returns true if the menu is selected and false otherwise
   *
   * @return true if the menu is selected and false otherwise
   */
  public boolean isSelected()
  {
    return super.isSelected();
  }

  /**
   * A helper method to handle setSelected calls from both mouse events and 
   * direct calls to setSelected.  Direct calls shouldn't expand the popup
   * menu and should select the JMenu even if it is disabled.  Mouse events
   * only select the JMenu if it is enabled and should expand the popup menu
   * associated with this JMenu.
   * @param selected whether or not the JMenu was selected
   * @param menuEnabled whether or not selecting the menu is "enabled".  This
   * is always true for direct calls, and is set to isEnabled() for mouse 
   * based calls.
   * @param showMenu whether or not to show the popup menu
   */
  private void setSelectedHelper(boolean selected, boolean menuEnabled, boolean showMenu)
  {
    // If menu is selected and enabled, activates the menu and 
    // displays associated popup.	
    if (selected && menuEnabled)
      {
	super.setArmed(true);
	super.setSelected(true);

        // FIXME: The popup menu should be shown on the screen after certain
        // number of seconds pass. The 'delay' property of this menu indicates
        // this amount of seconds. 'delay' property is 0 by default.
	if (isShowing())
	  {
	    fireMenuSelected();
            
	    int x = 0;
	    int y = 0;
            if (showMenu)
              if (menuLocation == null)
                {
                  // Calculate correct position of the popup. Note that location of the popup 
                  // passed to show() should be relative to the popup's invoker
                  if (isTopLevelMenu())
                    y = this.getHeight();
                  else
                    x = this.getWidth();
                  getPopupMenu().show(this, x, y);
                }
              else
                {
                  getPopupMenu().show(this, menuLocation.x, menuLocation.y);
                }
	  }
      }
    
    else
      {
	super.setSelected(false);
	super.setArmed(false);
	fireMenuDeselected();
        getPopupMenu().setVisible(false);
      }
  }

  /**
   * Changes this menu selected state if selected is true and false otherwise
   * This method fires menuEvents to menu's registered listeners.
   *
   * @param selected true if the menu should be selected and false otherwise
   */
  public void setSelected(boolean selected)
  {
    setSelectedHelper(selected, true, false); 
  }

  /**
   * Checks if PopupMenu associated with this menu is visible
   *
   * @return true if the popup associated with this menu is currently visible
   * on the screen and false otherwise.
   */
  public boolean isPopupMenuVisible()
  {
    return getPopupMenu().isVisible();
  }

  /**
   * Sets popup menu visibility
   *
   * @param popup true if popup should be visible and false otherwise
   */
  public void setPopupMenuVisible(boolean popup)
  {
    if (getModel().isEnabled())
      getPopupMenu().setVisible(popup);
  }

  /**
   * Returns origin point of the popup menu
   *
   * @return Point containing
   */
  protected Point getPopupMenuOrigin()
  {
    // if menu in the menu bar
    if (isTopLevelMenu())
      return new Point(0, this.getHeight());

    // if submenu            
    return new Point(this.getWidth(), 0);
  }

  /**
   * Returns delay property.
   *
   * @return delay property, indicating number of milliseconds before
   * popup menu associated with the menu appears or disappears after
   * menu was selected or deselected respectively
   */
  public int getDelay()
  {
    return delay;
  }

  /**
   * Sets delay property for this menu. If given time for the delay
   * property is negative, then IllegalArgumentException is thrown
   *
   * @param delay number of milliseconds before
   * popup menu associated with the menu appears or disappears after
   * menu was selected or deselected respectively
   */
  public void setDelay(int delay)
  {
    if (delay < 0)
      throw new IllegalArgumentException("delay less than 0");
    this.delay = delay;
  }

  /**
   * Sets location at which popup menu should be displayed
   * The location given is relative to this menu item
   *
   * @param x x-coordinate of the menu location
   * @param y y-coordinate of the menu location
   */
  public void setMenuLocation(int x, int y)
  {
    menuLocation = new Point(x, y);
  }

  /**
   * Creates and returns JMenuItem associated with the given action
   *
   * @param action Action to use for creation of JMenuItem
   *
   * @return JMenuItem that was creted with given action
   */
  protected JMenuItem createActionComponent(Action action)
  {
    return new JMenuItem(action);
  }

  /**
   * Creates ActionChangeListener to listen for PropertyChangeEvents occuring
   * in the action that is associated with this menu
   *
   * @param item menu that contains action to listen to
   *
   * @return The PropertyChangeListener
   */
  protected PropertyChangeListener createActionChangeListener(JMenuItem item)
  {
    return new ActionChangedListener(item);
  }

  /**
   * Adds separator to the end of the menu items in the menu.
   */
  public void addSeparator()
  {
    getPopupMenu().addSeparator();
  }

  /**
   * Inserts separator in the menu at the specified index.
   *
   * @param index Index at which separator should be inserted
   */
  public void insertSeparator(int index)
  {
    if (index < 0)
      throw new IllegalArgumentException("index less than 0");

    getPopupMenu().insert(new JPopupMenu.Separator(), index);
  }

  /**
   * Returns menu item located at the specified index in the menu
   *
   * @param index Index at which to look for the menu item
   *
   * @return menu item located at the specified index in the menu
   */
  public JMenuItem getItem(int index)
  {
    if (index < 0)
      throw new IllegalArgumentException("index less than 0");

    if (getItemCount() == 0)
      return null;
    
    Component c = popupMenu.getComponentAtIndex(index);

    if (c instanceof JMenuItem)
      return (JMenuItem) c;
    else
      return null;
  }

  /**
   * Returns number of items in the menu including separators.
   *
   * @return number of items in the menu
   *
   * @see #getMenuComponentCount()
   */
  public int getItemCount()
  {
    return getMenuComponentCount();
  }

  /**
   * Checks if this menu is a tear-off menu.
   *
   * @return true if this menu is a tear-off menu and false otherwise
   */
  public boolean isTearOff()
  {
    // NOT YET IMPLEMENTED 
    throw new Error("The method isTearOff() has not yet been implemented.");
  }

  /**
   * Returns number of menu components in this menu
   *
   * @return number of menu components in this menu
   */
  public int getMenuComponentCount()
  {
    return getPopupMenu().getComponentCount();
  }

  /**
   * Returns menu component located at the givent index
   * in the menu
   *
   * @param index index at which to get the menu component in the menu
   *
   * @return Menu Component located in the menu at the specified index
   */
  public Component getMenuComponent(int index)
  {
    if (getPopupMenu() == null || getMenuComponentCount() == 0)
      return null;
    
    return (Component) popupMenu.getComponentAtIndex(index);
  }

  /**
   * Return components belonging to this menu
   *
   * @return components belonging to this menu
   */
  public Component[] getMenuComponents()
  {
    return getPopupMenu().getComponents();
  }

  /**
   * Checks if this menu is a top level menu. The menu is top
   * level menu if it is inside the menu bar. While if the menu
   * inside some other menu, it is considered to be a pull-right menu.
   *
   * @return true if this menu is top level menu, and false otherwise
   */
  public boolean isTopLevelMenu()
  {
    return getParent() instanceof JMenuBar;
  }

  /**
   * Checks if given component exists in this menu. The submenus of
   * this menu are checked as well
   *
   * @param component Component to look for
   *
   * @return true if the given component exists in this menu, and false otherwise
   */
  public boolean isMenuComponent(Component component)
  {
    return false;
  }

  /**
   * Returns popup menu associated with the menu.
   *
   * @return popup menu associated with the menu.
   */
  public JPopupMenu getPopupMenu()
  {
    if (popupMenu == null)
      {
        popupMenu = new JPopupMenu();
        popupMenu.setInvoker(this);
      }
    return popupMenu;
  }

  /**
   * Adds MenuListener to the menu
   *
   * @param listener MenuListener to add
   */
  public void addMenuListener(MenuListener listener)
  {
    listenerList.add(MenuListener.class, listener);
  }

  /**
   * Removes MenuListener from the menu
   *
   * @param listener MenuListener to remove
   */
  public void removeMenuListener(MenuListener listener)
  {
    listenerList.remove(MenuListener.class, listener);
  }

  /**
   * Returns all registered <code>MenuListener</code> objects.
   *
   * @return an array of listeners
   * 
   * @since 1.4
   */
  public MenuListener[] getMenuListeners()
  {
    return (MenuListener[]) listenerList.getListeners(MenuListener.class);
  }

  /**
   * This method fires MenuEvents to all menu's MenuListeners. In this case
   * menuSelected() method of MenuListeners is called to indicated that the menu
   * was selected.
   */
  protected void fireMenuSelected()
  {
    MenuListener[] listeners = getMenuListeners();

    for (int index = 0; index < listeners.length; ++index)
      listeners[index].menuSelected(menuEvent);
  }

  /**
   * This method fires MenuEvents to all menu's MenuListeners. In this case
   * menuDeselected() method of MenuListeners is called to indicated that the menu
   * was deselected.
   */
  protected void fireMenuDeselected()
  {
    EventListener[] ll = listenerList.getListeners(MenuListener.class);

    for (int i = 0; i < ll.length; i++)
      ((MenuListener) ll[i]).menuDeselected(menuEvent);
  }

  /**
   * This method fires MenuEvents to all menu's MenuListeners. In this case
   * menuSelected() method of MenuListeners is called to indicated that the menu
   * was cancelled. The menu is cancelled when it's popup menu is close without selection.
   */
  protected void fireMenuCanceled()
  {
    EventListener[] ll = listenerList.getListeners(MenuListener.class);

    for (int i = 0; i < ll.length; i++)
      ((MenuListener) ll[i]).menuCanceled(menuEvent);
  }

  /**
   * Creates WinListener that listens to the menu;s popup menu.
   *
   * @param popup JPopupMenu to listen to
   *
   * @return The WinListener
   */
  protected WinListener createWinListener(JPopupMenu popup)
  {
    return new WinListener(popup);
  }

  /**
   * Method of the MenuElementInterface. It reacts to the selection
   * changes in the menu. If this menu was selected, then it
   * displayes popup menu associated with it and if this menu was
   * deselected it hides the popup menu.
   *
   * @param changed true if the menu was selected and false otherwise
   */
  public void menuSelectionChanged(boolean changed)
  {
    // if this menu selection is true, then activate this menu and 
    // display popup associated with this menu
    setSelectedHelper(changed, isEnabled(), true);
  }

  /**
   * Method of MenuElement interface. Returns sub components of
   * this menu.
   *
   * @return array containing popupMenu that is associated with this menu
   */
  public MenuElement[] getSubElements()
  {
    return new MenuElement[] { popupMenu };
  }

  /**
   * @return Returns reference to itself
   */
  public Component getComponent()
  {
    return this;
  }

  /**
   * This method is overriden with empty implementation, s.t the
   * accelerator couldn't be set for the menu. The mnemonic should
   * be used for the menu instead.
   *
   * @param keystroke accelerator for this menu
   */
  public void setAccelerator(KeyStroke keystroke)
  {
    throw new Error("setAccelerator() is not defined for JMenu.  Use setMnemonic() instead.");
  }

  /**
   * This method process KeyEvent occuring when the menu is visible
   *
   * @param event The KeyEvent
   */
  protected void processKeyEvent(KeyEvent event)
  {
    MenuSelectionManager.defaultManager().processKeyEvent(event);
  }

  /**
   * Programatically performs click
   *
   * @param time Number of milliseconds for which this menu stays pressed
   */
  public void doClick(int time)
  {
    getModel().setArmed(true);
    getModel().setPressed(true);
    try
      {
	java.lang.Thread.sleep(time);
      }
    catch (java.lang.InterruptedException e)
      {
	// probably harmless
      }

    getModel().setPressed(false);
    getModel().setArmed(false);
    popupMenu.show(this, this.getWidth(), 0);
  }

  /**
   * A string that describes this JMenu. Normally only used
   * for debugging.
   *
   * @return A string describing this JMenu
   */
  protected String paramString()
  {
    return super.paramString();
  }

  public AccessibleContext getAccessibleContext()
  {
    if (accessibleContext == null)
      accessibleContext = new AccessibleJMenu();

    return accessibleContext;
  }

  /**
   * Implements support for assisitive technologies for <code>JMenu</code>.
   */
  protected class AccessibleJMenu extends AccessibleJMenuItem
    implements AccessibleSelection
  {
    private static final long serialVersionUID = -8131864021059524309L;

    protected AccessibleJMenu()
    {
      // Nothing to do here.
    }

    /**
     * Returns the number of accessible children of this object.
     *
     * @return the number of accessible children of this object
     */
    public int getAccessibleChildrenCount()
    {
      Component[] children = getMenuComponents();
      int count = 0;
      for (int i = 0; i < children.length; i++)
        {
          if (children[i] instanceof Accessible)
            count++;
        }
      return count;
    }

    /**
     * Returns the accessible child with the specified <code>index</code>.
     *
     * @param index the index of the child to fetch
     *
     * @return the accessible child with the specified <code>index</code>
     */
    public Accessible getAccessibleChild(int index)
    {
      Component[] children = getMenuComponents();
      int count = 0;
      Accessible found = null;
      for (int i = 0; i < children.length; i++)
        {
          if (children[i] instanceof Accessible)
            {
              if (count == index)
                {
                  found = (Accessible) children[i];
                  break;
                }
              count++;
            }
        }
      return found;
    }

    /**
     * Returns the accessible selection of this object. AccessibleJMenus handle
     * their selection themselves, so we always return <code>this</code> here.
     *
     * @return the accessible selection of this object
     */
    public AccessibleSelection getAccessibleSelection()
    {
      return this;
    }

    /**
     * Returns the selected accessible child with the specified
     * <code>index</code>.
     *
     * @param index the index of the accessible selected child to return
     *
     * @return the selected accessible child with the specified
     *         <code>index</code>
     */
    public Accessible getAccessibleSelection(int index)
    {
      Accessible selected = null;
      // Only one item can be selected, which must therefore have index == 0.
      if (index == 0)
        {
          MenuSelectionManager msm = MenuSelectionManager.defaultManager();
          MenuElement[] me = msm.getSelectedPath();
          if (me != null)
            {
              for (int i = 0; i < me.length; i++)
                {
                  if (me[i] == JMenu.this)
                    {
                      // This JMenu is selected, find and return the next
                      // JMenuItem in the path.
                      do
                        {
                          if (me[i] instanceof Accessible)
                            {
                              selected = (Accessible) me[i];
                              break;
                            }
                          i++;
                        } while (i < me.length);
                    }
                  if (selected != null)
                    break;
                }
            }
        }
      return selected;
    }

    /**
     * Returns <code>true</code> if the accessible child with the specified
     * index is selected, <code>false</code> otherwise.
     *
     * @param index the index of the accessible child to check
     *
     * @return <code>true</code> if the accessible child with the specified
     *         index is selected, <code>false</code> otherwise
     */
    public boolean isAccessibleChildSelected(int index)
    {
      boolean selected = false;
      MenuSelectionManager msm = MenuSelectionManager.defaultManager();
      MenuElement[] me = msm.getSelectedPath();
      if (me != null)
        {
          Accessible toBeFound = getAccessibleChild(index);
          for (int i = 0; i < me.length; i++)
            {
              if (me[i] == toBeFound)
                {
                  selected = true;
                  break;
                }
            }
        }
      return selected;
    }

    /**
     * Returns the accessible role of this object, which is
     * {@link AccessibleRole#MENU} for the AccessibleJMenu.
     *
     * @return the accessible role of this object
     */
    public AccessibleRole getAccessibleRole()
    {
      return AccessibleRole.MENU;
    }

    /**
     * Returns the number of selected accessible children. This will be
     * <code>0</code> if no item is selected, or <code>1</code> if an item
     * is selected. AccessibleJMenu can have maximum 1 selected item.
     *
     * @return the number of selected accessible children
     */
    public int getAccessibleSelectionCount()
    {
      int count = 0;
      MenuSelectionManager msm = MenuSelectionManager.defaultManager();
      MenuElement[] me = msm.getSelectedPath();
      if (me != null)
        {
          for (int i = 0; i < me.length; i++)
            {
              if (me[i] == JMenu.this)
                {
                  if (i + 1 < me.length)
                    {
                      count = 1;
                      break;
                    }
                }
            }
        }
      return count;
    }

    /**
     * Selects the accessible child with the specified index.
     *
     * @param index the index of the accessible child to select
     */
    public void addAccessibleSelection(int index)
    {
      Accessible child = getAccessibleChild(index);
      if (child != null && child instanceof JMenuItem)
        {
          JMenuItem mi = (JMenuItem) child;
          MenuSelectionManager msm = MenuSelectionManager.defaultManager();
          msm.setSelectedPath(createPath(JMenu.this));
        }
    }

    /**
     * Removes the item with the specified index from the selection.
     *
     * @param index the index of the selected item to remove from the selection
     */
    public void removeAccessibleSelection(int index)
    {
      Accessible child = getAccessibleChild(index);
      if (child != null)
        {
          MenuSelectionManager msm = MenuSelectionManager.defaultManager();
          MenuElement[] oldSelection = msm.getSelectedPath();
          for (int i = 0; i < oldSelection.length; i++)
            {
              if (oldSelection[i] == child)
                {
                  // Found the specified child in the selection. Remove it
                  // from the selection.
                  MenuElement[] newSel = new MenuElement[i - 1];
                  System.arraycopy(oldSelection, 0, newSel, 0, i - 1);
                  msm.setSelectedPath(newSel);
                  break;
                }
            }
        }
    }

    /**
     * Removes all possibly selected accessible children of this object from
     * the selection.
     */
    public void clearAccessibleSelection()
    {
      MenuSelectionManager msm = MenuSelectionManager.defaultManager();
      MenuElement[] oldSelection = msm.getSelectedPath();
      for (int i = 0; i < oldSelection.length; i++)
        {
          if (oldSelection[i] == JMenu.this)
            {
              // Found this menu in the selection. Remove all children from
              // the selection.
              MenuElement[] newSel = new MenuElement[i];
              System.arraycopy(oldSelection, 0, newSel, 0, i);
              msm.setSelectedPath(newSel);
              break;
            }
        }
    }

    /**
     * AccessibleJMenu don't support multiple selection, so this method
     * does nothing.
     */
    public void selectAllAccessibleSelection()
    {
      // Nothing to do here.
    }
  }

  protected class WinListener extends WindowAdapter implements Serializable
  {
    private static final long serialVersionUID = -6415815570638474823L;

    /**
     * Creates a new <code>WinListener</code>.
     *
     * @param popup the popup menu which is observed
     */
    public WinListener(JPopupMenu popup)
    {
      // TODO: What should we do with the popup argument?
    }

    /**
     * Receives notification when the popup menu is closing and deselects
     * the menu.
     *
     * @param event the window event
     */
    public void windowClosing(WindowEvent event)
    {
      setSelected(false);
    }
  }

  /**
   * This class listens to PropertyChangeEvents occuring in menu's action
   */
  private class ActionChangedListener implements PropertyChangeListener
  {
    /** menu item associated with the action */
    private JMenuItem menuItem;

    /** Creates new ActionChangedListener and adds it to menuItem's action */
    public ActionChangedListener(JMenuItem menuItem)
    {
      this.menuItem = menuItem;

      Action a = menuItem.getAction();
      if (a != null)
	a.addPropertyChangeListener(this);
    }

    /**This method is invoked when some change occures in menuItem's action*/
    public void propertyChange(PropertyChangeEvent evt)
    {
      // FIXME: Need to implement
    }
  }

  /**
   * Creates an array to be feeded as argument to
   * {@link MenuSelectionManager#setSelectedPath(MenuElement[])} for the
   * specified element. This has the effect of selecting the specified element
   * and all its parents.
   *
   * @param leaf the leaf element for which to create the selected path
   *
   * @return the selected path array
   */
  MenuElement[] createPath(JMenu leaf)
  {
    ArrayList path = new ArrayList();
    MenuElement[] array = null;
    Component current = leaf.getPopupMenu();
    while (true)
      {
        if (current instanceof JPopupMenu)
          {
            JPopupMenu popupMenu = (JPopupMenu) current;
            path.add(0, popupMenu);
            current = popupMenu.getInvoker();
          }
        else if (current instanceof JMenu)
          {
            JMenu menu = (JMenu) current;
            path.add(0, menu);
            current = menu.getParent();
          }
        else if (current instanceof JMenuBar)
          {
            JMenuBar menuBar = (JMenuBar) current;
            path.add(0, menuBar);
            array = new MenuElement[path.size()];
            array = (MenuElement[]) path.toArray(array);
            break;
          }
      }
    return array;
  }
}
