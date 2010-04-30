/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Decorator;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

import java.lang.reflect.Type;
import java.util.Properties;

/**
 * BehaviorFactory for Decorating. This factory will create {@link org.picocontainer.behaviors.Decorating.Decorated} that will
 * allow you to decorate what you like on the component instance that has been created
 *
 * @author Paul Hammant
 */
public abstract class Decorating extends AbstractBehavior implements Decorator {

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties, final Object componentKey,
                                                   final Class<T> componentImplementation, final Parameter... parameters) throws PicoCompositionException {
        return componentMonitor.newBehavior(new Decorated<T>(super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                componentProperties,componentKey, componentImplementation, parameters), this));
    }


    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties, ComponentAdapter<T> adapter) {
        return super.addComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, adapter);
    }

    @SuppressWarnings("serial")
    public static class Decorated<T> extends AbstractChangedBehavior<T> {
        private final Decorator decorator;

        public Decorated(ComponentAdapter<T> delegate, Decorator decorator) {
            super(delegate);
            this.decorator = decorator;
        }

        public T getComponentInstance(final PicoContainer container, Type into)
                throws PicoCompositionException {
            T instance = super.getComponentInstance(container, into);
            decorator.decorate(instance);
            return instance;
        }

        public String getDescriptor() {
            return "Decorated";
        }

    }

}