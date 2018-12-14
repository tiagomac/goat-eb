package io.github.tiagomac.goateb;

import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



/**
 * implementação do EventBus
 *
 */
public class EventBus {

	private Map<Class<?>, Set<Invocation>> invocations;
	private String name;

	public EventBus(String name) {
		this.name = name;
		invocations = new ConcurrentHashMap<>();
	}

	public void post(Object object) {
		Class<?> clazz = object.getClass();
		if (invocations.containsKey(clazz)) {
			invocations.get(clazz).forEach(invocation -> invocation.invoke(object));
		}
	}

	public void register(Object object) {
		Class<?> currentClass = object.getClass();

		while (currentClass != null) {
			List<Method> subscribeMethods = findSubscriptionMethods(currentClass);
			for (Method method : subscribeMethods) {
				// Essa chamada deve ser modificada se for necessário mudar
				// a quantidade de parâmetros.
				Class<?> type = method.getParameterTypes()[0];
				if (invocations.containsKey(type)) {
					invocations.get(type).add(new Invocation(method, object));
				} else {
					Set<Invocation> temp = new HashSet<>();
					temp.add(new Invocation(method, object));
					invocations.put(type, temp);
				}
			}
			currentClass = currentClass.getSuperclass();
		}
	}

	public void unregister(Object object) {
		Class<?> currentClass = object.getClass();
		while (currentClass != null) {
			List<Method> subscribeMethods = findSubscriptionMethods(currentClass);
			for (Method method : subscribeMethods) {
				Class<?> type = method.getParameterTypes()[0];
				if (invocations.containsKey(type)) {
					Set<Invocation> invocationsSet = invocations.get(type);
					invocationsSet.remove(new Invocation(method, object));
					if (invocationsSet.isEmpty()) {
						invocations.remove(type);
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}
	}

	private List<Method> findSubscriptionMethods(Class<?> type) {
		List<Method> subscribeMethods = Arrays.stream(type.getDeclaredMethods()).filter(this::isSubscribed)
				.collect(Collectors.toList());
		return filterSingleParameterMethods(subscribeMethods);
	}

	private List<Method> filterSingleParameterMethods(List<Method> subscribeMethods) {
		return subscribeMethods.stream().filter(method -> method.getParameterCount() == 1).collect(Collectors.toList());
	}

	private boolean isSubscribed(Method method) {
		Subscribe[] subscribes = method.getAnnotationsByType(Subscribe.class);
		return Arrays.stream(subscribes).anyMatch(subscribe -> this.name.equals(subscribe.value()));
	}

	public Map<Class<?>, Set<Invocation>> getInvocations() {
		return invocations;
	}

	public String getName() {
		return name;
	}
}
