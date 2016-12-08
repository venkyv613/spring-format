/*
 * Copyright 2012-2016 the original author or authors.
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

package io.spring.format.formatter.intellij;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.intellij.openapi.project.Project;
import io.spring.format.formatter.intellij.Trigger.State;

/**
 * FIXME.
 *
 * @author Phillip Webb
 */
public abstract class Monitors {

	private final List<Monitor> monitors;

	private final List<State> states = new ArrayList<State>();

	public Monitors(Project project, Monitor.Factory... monitorFactories) {
		this(project, Arrays.asList(monitorFactories));
	}

	public Monitors(Project project, List<Monitor.Factory> monitorFactories) {
		List<Monitor> monitors = new ArrayList<>(monitorFactories.size());
		for (Monitor.Factory factory : monitorFactories) {
			monitors.add(factory.createMonitor(project, addTrigger()));
		}
		this.monitors = Collections.unmodifiableList(monitors);
	}

	private Trigger addTrigger() {
		int index = addState();
		return (state) -> {
			boolean activeBefore;
			boolean activeAfter;
			synchronized (this.states) {
				activeBefore = this.states.stream()
						.anyMatch(item -> item == State.ACTIVE);
				this.states.set(index, state);
				activeAfter = this.states.stream().anyMatch(item -> item == State.ACTIVE);
			}
			if (activeBefore != activeAfter) {
				changeState(activeAfter ? State.ACTIVE : State.NOT_ACTIVE);
			}
		};
	}

	private int addState() {
		synchronized (this.states) {
			this.states.add(State.NOT_ACTIVE);
			return this.states.size() - 1;
		}
	}

	public void stop() {
		for (Monitor monitor : this.monitors) {
			monitor.stop();
		}
	}

	protected abstract void changeState(State state);

}
