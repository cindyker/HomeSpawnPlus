/**
 * 
 */
package org.morganm.homespawnplus.storage;

/** Exceptions relating to backing storage. Usually wraps some other exception
 * type such as SQLException or IOException, to give us a consistent exception
 * interface.
 * 
 * @author morganm
 *
 */
public class StorageException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public StorageException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public StorageException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public StorageException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StorageException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
