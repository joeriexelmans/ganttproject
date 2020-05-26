package net.sourceforge.ganttproject.project;

/**
 * A "view" class for a project, i.e. a class that displays
 */
public interface ProjectOpenedState {
    public void openProject(IProject project);

    public void closeProject();

//    private IProject currentProject = null;
//
//    public final void openProject(IProject project) throws InvalidStateException {
//        if (currentProject != null) {
//            throw new InvalidStateException("Already opened, must close first");
//        }
//        onOpen(project);
//        currentProject = project;
//    }
//
//    public final void closeProject() throws InvalidStateException {
//        if (currentProject == null) {
//            throw new InvalidStateException("Already closed");
//        }
//        onClose();
//        currentProject = null;
//    }
//
//    public final IProject getCurrentProject() {
//        return currentProject;
//    }
//
//    abstract protected void onOpen(IProject project);
//
//    abstract protected void onClose();
//
//    public static class InvalidStateException extends Exception {
//        public InvalidStateException(String message) {
//            super(message);
//        }
//    }
}
