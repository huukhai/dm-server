/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2011 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.test.tools.ant;

import java.util.StringTokenizer;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;


/**
 * This class is a Jakarta Ant custom task that repeatedly calls a target
 * passing different values from a delimited string.
 *
 * <p>For example:
 * <pre>
 * &lt;project name="Test" default="main" basedir="."&gt;
 *   &lt;taskdef name="iterate" classname="com.funambol.test.tools.ant.IterateTask"/&gt;
 *
 *   &lt;target name="main"&gt;
 *     &lt;iterate target="hello" itemList="1,2,3" property="i" &gt;
 *     &lt;/iterate&gt;
 *   &lt;/target&gt;
 *
 *   &lt;target name="hello"&gt;
 *     &lt;echo message="${i}" /&gt;
 *   &lt;/target&gt;
 * &lt;/project&gt;
 * </pre>
 *
 * <p>The above example will call the hello target three times, each time
 * passing a value from the item list. In this case the hello target will
 * echo 1, then 2 and then 3.
 *
 * <p>A more useful example is the ability to compile multiple source
 * directories into multiple jar files. For example:
 *
 * <pre>
 * &lt;target name="build" depends="init"&gt;
 *
 *   &lt;!-- iterate through the ${build.modules} variable, compiling each module specified --&gt;
 *   &lt;iterate target="javac" itemList="myModule,myModule/mySubModule" property="iterate.module"/&gt;
 * &lt;/target&gt;
 *
 * &lt;target name="javac" depends="checkSource, cleanBuild, prepareBuild" if="compile.source.exist"&gt;
 *
 *   &lt;javac  srcdir="${iterate.module}/src" destdir="${iterate.module}/build"&gt;
 *     &lt;include name="**\/*.java"/&gt;
 *   &lt;/javac&gt;
 *
 *   &lt;!-- create a jar file for each module--&gt;
 *   &lt;mkdir dir="${iterate.module}/lib"/&gt;
 *   &lt;jar jarfile="${iterate.module}/lib/classes.jar"&gt;
 *     &lt;fileset dir="${iterate.module}/build"/&gt;
 *   &lt;/jar&gt;
 * &lt;/target&gt;
 * </pre>
 *
 * <p>The about example does the following:
 * <ul>
 *   <li>compiles the myModule/src directory into myModule/lib/classes.jar
 *   <li>compiles the myModule/mySubModule/src directory into myModule/mySubModule/lib/classes.jar
 * <ul>
 *
 * <p>List of attributes:
 * <table border>
 * <tr><th>Attribute</th><th>Description</th><th>Required</th></tr>
 * <tr><td>target</td><td>This is the name of the target to call.</td><td>Yes</td></tr>
 * <tr><td>property</td><td>The name of the property in which each value from the item list will be stored for each iteration. This allows the called target to access each item from the item list.</td><td>Yes</td></tr>
 * <tr><td>items</td><td>A delimited list of items strings.</td><td>Yes</td></tr>
 * <tr><td>iheritAll</td><td>Boolean to enable/disable the called target from inheriting all the properties from the environment.</td><td>No</td></tr>
 * <tr><td>delimiter</td><td>The delimiter that is used to delimited the strings in the item list.</td><td>No</td></tr>
 * </table>
 * 
 * @version $Id: IterateTask.java,v 1.2 2006/08/07 21:09:26 nichele Exp $
 */
public class IterateTask extends Task {

  /** Default constructor.
   */
  public IterateTask() {}

  /** Set the item list string. The item list can contain one or more
   *  delimited strings. The specified target will be called once for each
   *  string in the item list.
   *
   *  <p>The delimiter can be changed by specifying the
   *  delimiter attribute.
   *
   * @param items Delimited string of items.
   */
  public void setItems(String items) {
      this.items = items;
  }

  /** Set the Ant target name that will be called repeatedly, once for each
   *  item in the item list.
   *
   * @param targetName The name of the target to call repeatedly.
   */
  public void setTarget(String targetName) {
    this.targetName = targetName;
  }

  /** Sets the inherit all flag. If the value is true, then the target that
   *  is called will inherit all the properties. Default is true.
   *
   * @param inheritAll Inherit flag.
   */
  public void setInheritAll(boolean inheritAll) {
    this.inheritAll = inheritAll;
  }

  /** Set the Property. The property attribute is the name of the property
   *  that will contain each item in the item list.
   *
   * @param property Property Name
   */
  public void setProperty(String property) {
    this.property = property;
  }

  /** Set the delimiter that will be used to delimit the strings in the item
   *  list, the default is comma ",".
   *
   * @param delimiter Delimiter charater.
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  /** Ant execute service method, called when the task is executed.
   *
   * @exception BuildException When required attributes are not set
   */
  public void execute() throws BuildException {
    Project p = getProject();

    validateAttributes();

    // initialise the target
    task.setDir(p.getBaseDir());
    task.setAntfile(p.getProperty("ant.file"));
    task.setTarget(targetName);
    task.setInheritAll(inheritAll);

    // call the target for each item in the item list
    StringTokenizer st = new StringTokenizer(items, delimiter);
    while (st.hasMoreTokens()) {
      p.setProperty(property, (st.nextToken().trim()));
      task.execute();
    }
  }

  /** Ant init service method, to initialise the task.
   *
   * @exception BuildException Build exception.
   */
  public void init() throws BuildException {
    super.init();

    // initialise the called target
    task = (Ant) getProject().createTask("ant");
    task.setOwningTarget(getOwningTarget());
    task.setTaskName(targetName);
    task.setLocation(getLocation());
    task.init();
  }

  /** Ant create param service method, which is called for each embedded param
   *  element.
   *
   * @return Property object.
   */
  public Property createParam() {
    return task.createProperty();
  }


  // ----------------------------------------------------------- Private methods

  private void validateAttributes() throws BuildException {
      if (isEmpty(targetName)) {
          throw new BuildException("Attribute target is required.", getLocation());
      }

      if (isEmpty(property)) {
          throw new BuildException("Attribute property is required.", getLocation());
      }

      if (items == null) {
          throw new BuildException("Attribute items is required.", getLocation());
      }
  }

  private boolean isEmpty(String s) {
      return ((s == null) || (s.length() == 0));
  }

  // -------------------------------------------------------------- Private data

  private String items;
  private String targetName;
  private boolean inheritAll = true;
  private Ant task;
  private String property;
  private String delimiter = ",";

}
