/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.gems.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.gems.GemsCharacteristics;

import java.lang.reflect.Type;
import java.util.Properties;


/**
 * Hides implementation.
 * 
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @see HotSwappable
 */
@SuppressWarnings("serial")
public class HotSwapping extends AbstractBehavior {


	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class<T> componentImplementation, final Parameter... parameters)
            throws PicoCompositionException {
        ComponentAdapter<T> delegateAdapter = super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                componentProperties, componentKey, componentImplementation, parameters);

        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_HOT_SWAP)) {
        	return delegateAdapter;
		} 

		AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.HOT_SWAP);
        return componentMonitor.newBehavior(new HotSwappable<T>(delegateAdapter));
    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor componentMonitor,
                                                final LifecycleStrategy lifecycleStrategy,
                                                final Properties componentProperties,
                                                final ComponentAdapter<T> adapter) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_HOT_SWAP)) {
        	return super.addComponentAdapter(componentMonitor,
                    lifecycleStrategy,
                    componentProperties,
                    adapter);
		} 

    	
		AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.HOT_SWAP);
    	return componentMonitor.newBehavior(new HotSwappable<T>(super.addComponentAdapter(componentMonitor,
                                                                 lifecycleStrategy,
                                                                 componentProperties,
                                                                 adapter)));
    }

    /**
     * This component adapter makes it possible to hide the implementation of a real subject (behind a proxy). If the key of the
     * component is of type {@link Class} and that class represents an interface, the proxy will only implement the interface
     * represented by that Class. Otherwise (if the key is something else), the proxy will implement all the interfaces of the
     * underlying subject. In any case, the proxy will also implement {@link com.thoughtworks.proxy.toys.hotswap.Swappable}, making
     * it possible to swap out the underlying subject at runtime. <p/> <em>
     * Note that this class doesn't cache instances. If you want caching,
     * use a {@link org.picocontainer.behaviors.Caching.Cached} around this one.
     * </em>
     *
     * @author Paul Hammant
     */
    @SuppressWarnings("serial")
    public static class HotSwappable<T> extends AsmImplementationHiding.AsmHiddenImplementation<T> {

        private final Swappable swappable = new Swappable();

        private T instance;

        public HotSwappable(final ComponentAdapter<T> delegate) {
            super(delegate);
        }

        @Override
        protected Swappable getSwappable() {
            return swappable;
        }

        @SuppressWarnings("unchecked")
        public T swapRealInstance(final T instance) {
            return (T) swappable.swap(instance);
        }

        @SuppressWarnings("unchecked")
        public T getRealInstance() {
            return (T) swappable.getInstance();
        }


        @Override
        public T getComponentInstance(final PicoContainer container, final Type into) {
            synchronized (swappable) {
                if (instance == null) {
                    instance = super.getComponentInstance(container, into);
                }
            }
            return instance;
        }

        @Override
        public String getDescriptor() {
            return "HotSwappable";
        }

        public static class Swappable {

            private transient Object delegate;

            public Object getInstance() {
                return delegate;
            }

            public Object swap(final Object delegate) {
                Object old = this.delegate;
                this.delegate = delegate;
                return old;
            }

        }

    }
}