/*
 * Copyright 2013 OmniFaces.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnifaces.application;

import java.util.logging.Logger;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ApplicationWrapper;

/**
 * This application factory takes care that the {@link OmniApplication} is properly initialized.
 *
 * @author Radu Creanga {@literal <rdcrng@gmail.com>}
 * @author Bauke Scholtz
 * @see OmniApplication
 * @since 1.6
 */
public class OmniApplicationFactory extends ApplicationFactory {

	// Private constants ----------------------------------------------------------------------------------------------

	private static final Logger logger = Logger.getLogger(OmniApplicationFactory.class.getName());

	private static final String WARNING_BAD_APPLICATION =
		"There is an Application implementation in the chain which does not properly extend from ApplicationWrapper:"
			+ " '%s'. This may potentially result in compatibility problems."
			+ " See also among others https://github.com/omnifaces/omnifaces/issues/75";

	// Variables ------------------------------------------------------------------------------------------------------

	private final ApplicationFactory wrapped;
	private volatile Application application;

	// Constructors ---------------------------------------------------------------------------------------------------

	/**
	 * Construct a new OmniFaces application factory around the given wrapped factory.
	 * @param wrapped The wrapped factory.
	 */
	public OmniApplicationFactory(ApplicationFactory wrapped) {
		this.wrapped = wrapped;
	}

	// Actions --------------------------------------------------------------------------------------------------------

	/**
	 * Returns an instance of {@link OmniApplication} which wraps the original application.
	 */
	@Override
	public Application getApplication() {
		return (application == null) ? createOmniApplication(wrapped.getApplication()) : application;
	}

	/**
	 * Sets the given application instance as the current instance. If it's not an instance of {@link OmniApplication},
	 * nor wraps the {@link OmniApplication}, then it will be wrapped by a new instance of {@link OmniApplication}.
	 */
	@Override
	public void setApplication(Application application) {
		wrapped.setApplication(createOmniApplication(application));
	}

	/**
	 * Returns the wrapped factory.
	 */
	@Override
	public ApplicationFactory getWrapped() {
		return wrapped;
	}

	// Helpers --------------------------------------------------------------------------------------------------------

	/**
	 * If the given application not an instance of {@link OmniApplication}, nor wraps the {@link OmniApplication}, then
	 * it will be wrapped by a new instance of {@link OmniApplication} and set as the current instance and returned.
	 */
	private synchronized Application createOmniApplication(final Application application) {
		Application newApplication = application;

		while (!(newApplication instanceof OmniApplication) && newApplication instanceof ApplicationWrapper) {
			newApplication = ((ApplicationWrapper) newApplication).getWrapped();
		}

		if (!(newApplication instanceof OmniApplication)) {
			if (!(newApplication instanceof ApplicationWrapper)) {
				logger.warning(String.format(WARNING_BAD_APPLICATION, newApplication.getClass().getName()));
			}

			newApplication = new OmniApplication(application);
		}

		this.application = newApplication;
		return this.application;
	}

}