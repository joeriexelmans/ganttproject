/*
GanttProject is an opensource project management tool.
Copyright (C) 2003-2010 Alexandre Thomas, Michael Barmeier, Dmitry Barashev

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.resource;

import biz.ganttproject.core.time.GanttCalendar;
import com.google.common.collect.Lists;
import net.sourceforge.ganttproject.CustomPropertyManager;
import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.undo.GPUndoManager;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author barmeier
 */
public class HumanResourceManager {

  public abstract static class ResourceBuilder {
    String myName;
    Integer myID;
    String myEmail;
    String myPhone;
    String myRole;
    BigDecimal myStandardRate;

    public ResourceBuilder withName(String name) {
      myName = name;
      return this;
    }

    public ResourceBuilder withID(String id) {
      myID = Integer.valueOf(id);
      return this;
    }

    public ResourceBuilder withEmail(String email) {
      myEmail = email;
      return this;
    }

    public ResourceBuilder withPhone(String phone) {
      myPhone = phone;
      return this;
    }

    public ResourceBuilder withRole(String role) {
      myRole = role;
      return this;
    }

    public ResourceBuilder withStandardRate(String rate) {
      if (rate != null) {
        try {
          myStandardRate = new BigDecimal(rate);
        } catch (NumberFormatException e) {
          myStandardRate = null;
        }
      }
      return this;
    }

    public abstract HumanResource build();
  }

  private List<ResourceListener> myListener = new ArrayList<ResourceListener>();

  private List<HumanResource> resources = new ArrayList<HumanResource>();

  private int nextFreeId = 0;

  private final AssignmentManager myAssignmentManager;

  private final Role myDefaultRole;

  private final CustomPropertyManager myCustomPropertyManager;

  private final RoleManager myRoleManager;

  public HumanResourceManager(AssignmentManager assignmentManager, Role defaultRole, CustomPropertyManager customPropertyManager) {
    this(assignmentManager, defaultRole, customPropertyManager, null);
  }

  public HumanResourceManager(AssignmentManager assignmentManager, Role defaultRole, CustomPropertyManager customPropertyManager, RoleManager roleManager) {
    myAssignmentManager = assignmentManager;
    myDefaultRole = defaultRole;
    myCustomPropertyManager = customPropertyManager;
    myRoleManager = roleManager;
  }

  public HumanResource newHumanResource() {
    HumanResource result = new HumanResource(this, myAssignmentManager);
    result.setRole(myDefaultRole);
    return result;
  }

  public ResourceBuilder newResourceBuilder() {
    return new ResourceBuilder() {

      @Override
      public HumanResource build() {
        if (myName == null || myID == null) {
          return null;
        }
        HumanResource result = new HumanResource(myName, myID, HumanResourceManager.this, myAssignmentManager);
        Role role = null;
        if (myRole != null && myRoleManager != null) {
          role = myRoleManager.getRole(myRole);
        }
        if (role == null) {
          role = myDefaultRole;
        }
        result.setRole(role);
        result.setPhone(myPhone);
        result.setMail(myEmail);
        result.setStandardPayRate(myStandardRate);
        add(result);
        return result;
      }

    };
  }
  public HumanResource create(String name, int i) {
    HumanResource hr = new HumanResource(name, i, this, myAssignmentManager);
    hr.setRole(myDefaultRole);
    add(hr);
    return hr;
  }

  public void add(HumanResource resource) {
    if (resource.getId() == -1) {
      resource.setId(nextFreeId);
    }
    if (resource.getId() >= nextFreeId) {
      nextFreeId = resource.getId() + 1;
    }
    resources.add(resource);
    fireResourceAdded(resource);
  }

  public HumanResource getById(int id) {
    // Linear search is not really efficient, but we do not have so many
    // resources !?
    HumanResource pr = null;
    for (int i = 0; i < resources.size(); i++)
      if (resources.get(i).getId() == id) {
        pr = resources.get(i);
        break;
      }
    return pr;
  }

  public List<HumanResource> getResources() {
    return resources;
  }

  public HumanResource[] getResourcesArray() {
    return resources.toArray(new HumanResource[resources.size()]);
  }

  public void remove(HumanResource resource) {
    fireResourcesRemoved(new HumanResource[] { resource });
    resources.remove(resource);
    myAssignmentManager.clearResourceAssignment(resource);
  }

  public void remove(HumanResource resource, GPUndoManager myUndoManager) {
    final HumanResource res = resource;
    myUndoManager.undoableEdit("Delete Human OK", new Runnable() {
      @Override
      public void run() {
        fireResourcesRemoved(new HumanResource[] { res });
        resources.remove(res);
        myAssignmentManager.clearResourceAssignment(res);
      }
    });
  }

  public void save(OutputStream target) {
  }

  public void clear() {
    fireCleanup();
    resources.clear();
  }

  public void addListener(ResourceListener listener) {
    myListener.add(listener);
  }

  private void fireResourceAdded(HumanResource resource) {
    ResourceEvent e = new ResourceEvent(this, resource);
    for (Iterator<ResourceListener> i = myListener.iterator(); i.hasNext();) {
      ResourceListener nextView = i.next();
      nextView.resourceAdded(e);
    }
  }

  void fireResourceChanged(HumanResource resource) {
    ResourceEvent e = new ResourceEvent(this, resource);
    for (Iterator<ResourceListener> i = myListener.iterator(); i.hasNext();) {
      ResourceListener nextView = i.next();
      nextView.resourceChanged(e);
    }
  }

  private void fireResourcesRemoved(HumanResource[] resources) {
    ResourceEvent e = new ResourceEvent(this, resources);
    for (int i = 0; i < myListener.size(); i++) {
      ResourceListener nextView = myListener.get(i);
      nextView.resourcesRemoved(e);
    }
  }

  public void fireAssignmentsChanged(HumanResource resource) {
    ResourceEvent e = new ResourceEvent(this, resource);
    for (Iterator<ResourceListener> i = myListener.iterator(); i.hasNext();) {
      ResourceListener nextView = i.next();
      nextView.resourceAssignmentsChanged(e);
    }
  }

  private void fireCleanup() {
    fireResourcesRemoved(resources.toArray(new HumanResource[resources.size()]));
  }

  /** Move up the resource number index */
  public void up(HumanResource hr) {
    int index = resources.indexOf(hr);
    assert index >= 0;
    resources.remove(index);
    resources.add(index - 1, hr);
    fireResourceChanged(hr);
  }

  /** Move down the resource number index */
  public void down(HumanResource hr) {
    int index = resources.indexOf(hr);
    assert index >= 0;
    resources.remove(index);
    resources.add(index + 1, hr);
    fireResourceChanged(hr);

  }

  public Map<HumanResource, HumanResource> importData(HumanResourceManager hrManager, HumanResourceMerger merger) {
    Map<HumanResource, HumanResource> foreign2native = new HashMap<HumanResource, HumanResource>();
    List<HumanResource> foreignResources = hrManager.getResources();
    List<HumanResource> createdResources = Lists.newArrayList();
    for (int i = 0; i < foreignResources.size(); i++) {
      HumanResource foreignHR = foreignResources.get(i);
      HumanResource nativeHR = merger.findNative(foreignHR, this);
      if (nativeHR == null) {
        nativeHR = new HumanResource(foreignHR.getName(), nextFreeId + createdResources.size(), this, myAssignmentManager);
        nativeHR.setRole(myDefaultRole);
        createdResources.add(nativeHR);
      }
      foreign2native.put(foreignHR, nativeHR);
    }
    for (HumanResource created : createdResources) {
      add(created);
    }
    merger.merge(foreign2native);
    return foreign2native;
  }

  public CustomPropertyManager getCustomPropertyManager() {
    return myCustomPropertyManager;
  }

  static String getValueAsString(Object value) {
    final String result;
    if (value != null) {
      if (value instanceof GanttCalendar) {
        result = ((GanttCalendar) value).toXMLString();
      } else {
        result = String.valueOf(value);
      }
    } else {
      result = null;
    }
    return result;
  }
}