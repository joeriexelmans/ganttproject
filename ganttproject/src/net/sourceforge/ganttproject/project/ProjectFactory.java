package net.sourceforge.ganttproject.project;

/**
 * The reason for this interface is to abstract away the dependencies of creating a new blank project.
 */
public interface ProjectFactory {
    /**
     * Create a new blank project.
     */
    public Project newProject();
}
