// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.mail;

/**
 * Create {@link ReceiveMailSession email receiving sessions}.
 */
public interface ReceiveMailSessionProvider {

	/**
	 * Creates new receiving mail session.
	 */
	ReceiveMailSession createSession();
}
