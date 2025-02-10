package site.musclebeaver.album.api.exception;

public class FolderAlreadyExistsException extends RuntimeException {

    public FolderAlreadyExistsException(String message) {
        super(message);
    }
}
