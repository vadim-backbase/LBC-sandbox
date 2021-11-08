package com.backbase.accesscontrol.business.flows;

/**
 * Abstract flow that supports defines pre and post-hook methods and ensures invocation of these. A flow class
 * encapsulates a particular business flow. An abstract flow class like this could be provided by S-SDK.
 *
 * <p>POC for replacing the Camel based service extension with something easier to use on the project. <br> What this
 * would accomplish: <ul> <li>Remove some camel magic. <li>Easier to extend when this class is extended and selected
 * methods overridden in a service extension. <li>Type safe service extensions! <li>Nicer to create documentation that
 * can be navigated. </ul>
 *
 * @param <I> The type of the flow input.
 * @param <O> The type of the flow output.
 */
public abstract class AbstractFlow<I, O> {

    /**
     * Starts the flow and will invoke preHook, execute and postHook in order.
     *
     * @param data the request data.
     * @return the result of the flow
     */
    public final O start(I data) {
        // TODO conditional on tenant enabled
        preHook(data);
        O result = execute(data);
        return postHook(data, result);
    }

    /**
     * Should be implemented in subclass to perform the actual flow.
     *
     * @param data This input for the flow.
     * @return The outcome of the flow.
     */
    protected abstract O execute(I data);

    /**
     * Invoked before {@link #execute(Object)}  is invoked. This implementation does nothing.
     *
     * @param data The request data.
     */
    protected void preHook(I data) {
        // do nothing
    }

    /**
     * Invoked after {@link #execute(Object)} allowing a sub class to post process the result of execute,
     * possibly modifying or replacing the result completely. This method simply returns <code>result</code>.
     *
     * @param data The original request data.
     * @param result The result as retured by {@link #execute(Object)}
     * @return Enriched, modified or replaced result.
     */
    protected O postHook(I data, O result) {
        return result;
    }
}
