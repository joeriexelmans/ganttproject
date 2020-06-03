/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject Team

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
package net.sourceforge.ganttproject.task;

import biz.ganttproject.core.time.GanttCalendar;
import com.google.common.base.Preconditions;
import net.sourceforge.ganttproject.assignment.AssignmentManager;
import net.sourceforge.ganttproject.assignment.LocalAssignment;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class ResourceAssignmentCollectionImpl implements ResourceAssignmentCollection {
  private final AssignmentManager myAssignmentManager;
//  private final Map<HumanResource, LocalAssignment> myAssignments = new LinkedHashMap<HumanResource, LocalAssignment>();

  private final TaskImpl myTask;

  private HumanResourceManager myResourceManager;

  public ResourceAssignmentCollectionImpl(AssignmentManager assignmentManager, TaskImpl task, HumanResourceManager resourceManager) {
    myAssignmentManager = assignmentManager;
    myTask = task;
    myResourceManager = resourceManager;
  }

  private ResourceAssignmentCollectionImpl(ResourceAssignmentCollectionImpl other) {
    myAssignmentManager = other.myAssignmentManager;
    myTask = other.myTask;
    myResourceManager = other.myResourceManager;
//    LocalAssignment[] assignments = other.getAssignments();
//    for (int i = 0; i < assignments.length; i++) {
//      LocalAssignment next = assignments[i];
//      LocalAssignment copy = new LocalAssignmentImpl(next.getResource());
//      copy.setLoad(next.getLoad());
//      copy.setCoordinator(next.isCoordinator());
//      copy.setRoleForAssignment(next.getRoleForAssignment());
//      addAssignment(copy);
//    }
  }

  @Override
  public void clear() {
    myAssignmentManager.clearTaskAssignments(myTask);
  }

  @Override
  public LocalAssignment[] getAssignments() {
    return myAssignmentManager.getTaskAssignments(myTask).toArray(LocalAssignment[]::new);
  }

  @Override
  public LocalAssignment getAssignment(HumanResource resource) {
    return myAssignmentManager.getAssignment(myTask, resource);
  }

//  @Override
//  public AssignmentManager.Mutator createMutator() {
//    return myAssignmentManager.createMutator();
////    return new ResourceAssignmentMutatorImpl();
//  }

  ResourceAssignmentCollectionImpl copy() {
    return new ResourceAssignmentCollectionImpl(this);
  }

  @Override
  public LocalAssignment addAssignment(HumanResource resource) {
    return myAssignmentManager.createAssignment(myTask, resource);
//    return auxAddAssignment(resource);
  }
//
  @Override
  public void deleteAssignment(HumanResource resource) {
    myAssignmentManager.removeAssignment(myTask, resource);
//    myAssignments.remove(resource);
  }
//
//  private LocalAssignment auxAddAssignment(HumanResource resource) {
//    final LocalAssignment result = new LocalAssignmentImpl(resource);
//    addAssignment(result);
//    return result;
//  }
//
//  private void addAssignment(LocalAssignment assignment) {
//    myAssignments.put(assignment.getResource(), assignment);
//  }

//  private class LocalAssignmentImpl implements LocalAssignment {
//    private LocalAssignment myAssignmentToResource;
//
//    public LocalAssignmentImpl(HumanResource resource) {
//      myAssignmentToResource = resource.createAssignment(this);
//      // resource.setAssignmentCollection(ResourceAssignmentCollectionImpl.this);
//    }
//
//    @Override
//    public Task getTask() {
//      return myTask;
//    }
//
//    @Override
//    public GanttCalendar getStart() {
//      return myTask.getStart();
//    }
//
//    @Override
//    public GanttCalendar getEnd() {
//      return myTask.getEnd();
//    }
//
//    @Override
//    public HumanResource getResource() {
//      return myAssignmentToResource.getResource();
//    }
//
//    @Override
//    public float getLoad() {
//      return myAssignmentToResource.getLoad();
//    }
//
//    @Override
//    public void setLoad(float load) {
//      myAssignmentToResource.setLoad(load);
//    }
//
//    /**
//     * Deletes all the assignments and all the related assignments
//     */
//    @Override
//    public void delete() {
//      ResourceAssignmentCollectionImpl.this.deleteAssignment(getResource());
//      myAssignmentToResource.delete();
//    }
//
//    @Override
//    public void setCoordinator(boolean responsible) {
//      myAssignmentToResource.setCoordinator(responsible);
//    }
//
//    @Override
//    public boolean isCoordinator() {
//      return myAssignmentToResource.isCoordinator();
//    }
//
//    @Override
//    public Role getRoleForAssignment() {
//      return myAssignmentToResource.getRoleForAssignment();
//    }
//
//    @Override
//    public void setRoleForAssignment(Role role) {
//      myAssignmentToResource.setRoleForAssignment(role);
//
//    }
//
//    @Override
//    public String toString() {
//      return this.getResource().getName() + " -> " + this.getTask().getName();
//    }
//  }

//  private class LocalAssignmentStub implements LocalAssignment {
//    private final HumanResource myResource;
//    private final Runnable myOnDelete;
//
//    private float myLoad;
//    private boolean myCoordinator;
//    private Role myRoleForAssignment;
//
//    public LocalAssignmentStub(HumanResource resource, Runnable onDelete) {
//      myResource = resource;
//      myOnDelete = onDelete;
//    }
//
//    @Override
//    public Task getTask() {
//      return myTask;
//    }
//
//    @Override
//    public GanttCalendar getStart() {
//      return myTask.getStart();
//    }
//
//    @Override
//    public GanttCalendar getEnd() {
//      return myTask.getEnd();
//    }
//
//    @Override
//    public HumanResource getResource() {
//      return myResource;
//    }
//
//    @Override
//    public float getLoad() {
//      return myLoad;
//    }
//
//    @Override
//    public void setLoad(float load) {
//      myLoad = load;
//    }
//
////    @Override
////    public void delete() {
////      myOnDelete.run();
////    }
//
//    @Override
//    public void setCoordinator(boolean responsible) {
//      myCoordinator = responsible;
//    }
//
//    @Override
//    public boolean isCoordinator() {
//      return myCoordinator;
//    }
//
//    @Override
//    public Role getRoleForAssignment() {
//
//      return myRoleForAssignment;
//    }
//
//    @Override
//    public void setRoleForAssignment(Role role) {
//      myRoleForAssignment = role;
//
//    }
//
//    @Override
//    public String toString() {
//      return this.getResource().getName() + " -> " + this.getTask().getName();
//    }
//  }

//  private class ResourceAssignmentMutatorImpl implements ResourceAssignmentMutator {
//    private Map<HumanResource, MutationInfo> myQueue = new HashMap<HumanResource, MutationInfo>();
//    private boolean invalidated = false;
//
//    @Override
//    public LocalAssignment addAssignment(final HumanResource resource) {
//      Preconditions.checkState( ! invalidated );
//
//      LocalAssignment result = new LocalAssignmentStub(resource, () -> myQueue.remove(resource));
//      myQueue.put(resource, new MutationInfo(result, MutationInfo.ADD));
//      return result;
//    }
//
//    @Override
//    public void deleteAssignment(HumanResource resource) {
//      Preconditions.checkState( ! invalidated );
//
//      MutationInfo info = myQueue.get(resource);
//      if (info == null) {
//        myQueue.put(resource, new MutationInfo(resource, MutationInfo.DELETE));
//      } else if (info.myOperation == MutationInfo.ADD) {
//        myQueue.remove(resource);
//      }
//    }
//
//    @Override
//    public void commit() {
//      Preconditions.checkState( ! invalidated );
//
//      List<MutationInfo> mutations = new ArrayList<MutationInfo>(myQueue.values());
//      Collections.sort(mutations);
//      for (int i = 0; i < mutations.size(); i++) {
//        MutationInfo next = mutations.get(i);
//        switch (next.myOperation) {
//        case MutationInfo.DELETE: {
//          myAssignments.remove(next.myResource);
//          break;
//        }
//        case MutationInfo.ADD: {
//          LocalAssignment result = auxAddAssignment(next.myResource);
//          result.setLoad(next.myAssignment.getLoad());
//          result.setCoordinator(next.myAssignment.isCoordinator());
//          result.setRoleForAssignment(next.myAssignment.getRoleForAssignment());
//        }
//        default:
//          break;
//        }
//      }
//      invalidated = true;
//    }
//
//  }

//  private static class MutationInfo implements Comparable<MutationInfo> {
//    static final int ADD = 0;
//
//    static final int DELETE = 1;
//
//    private final LocalAssignment myAssignment;
//
//    private final int myOrder;
//
//    private static int ourOrder;
//
//    private int myOperation;
//
//    private final HumanResource myResource;
//
//    public MutationInfo(LocalAssignment assignment, int operation) {
//      myAssignment = assignment;
//      myOrder = ourOrder++;
//      myOperation = operation;
//      myResource = assignment.getResource();
//    }
//
//    public MutationInfo(HumanResource resource, int operation) {
//      this.myAssignment = null;
//      this.myOrder = ourOrder++;
//      this.myOperation = operation;
//      this.myResource = resource;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//      boolean result = o instanceof MutationInfo;
//      if (result) {
//        result = myAssignment.getResource().equals(((MutationInfo) o).myAssignment.getResource());
//      }
//      return result;
//    }
//
//    @Override
//    public int compareTo(MutationInfo o) {
//      if (!(o instanceof MutationInfo)) {
//        throw new IllegalArgumentException();
//      }
//      return myOrder - o.myOrder;
//    }
//  }

  @Override
  public HumanResource getCoordinator() {
    for (LocalAssignment assignment: myAssignmentManager.getTaskAssignments(myTask)) {
      if (assignment.isCoordinator()) {
        return assignment.getResource();
      }
    }
//    for (Iterator<LocalAssignment> assignments = myAssignments.values().iterator(); assignments.hasNext();) {
//      LocalAssignment next = assignments.next();
//      if (next.isCoordinator()) {
//        return next.getResource();
//      }
//    }
    return null;
  }
}
