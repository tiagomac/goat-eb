package io.github.tiagomac.goateb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Nossa classe Wrapper, contém o método para invocar a classe alvo
 *
 */
public final class Invocation {

	private final Method handler;
	private final Object targetObject;

	public Invocation(Method handler, Object targetObject) {
		this.handler = handler;
		this.targetObject = targetObject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Invocation invoc = (Invocation) o;
		return Objects.equals(handler, invoc.handler) && Objects.equals(targetObject, invoc.targetObject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(handler, targetObject);
	}

	public void invoke(Object object) {
		try {
			handler.invoke(targetObject, object);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
