package io.github.tiagomac.goateb;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.tiagomac.goateb.EventBus;
import io.github.tiagomac.goateb.Subscribe;

class TestEventBus {

	private EventBus bus;

	private EventBus anotherBus;

	private HandlerClass mockedHandler;

	@BeforeEach
	void initBus() {
		bus = new EventBus("GoatEventBus");
		anotherBus = new EventBus("AnotherGuavaEventBus");

		mockedHandler = Mockito.mock(HandlerClass.class);
		bus.register(mockedHandler);
		anotherBus.register(mockedHandler);
	}

	@Test
	void postEventObjectSingleBus() {
		verifyCallsOnBus(bus);
	}

	private void verifyCallsOnBus(EventBus bus) {
		bus.post(new EventObject("Algum evento"));
		bus.post(new EventObject("Algum evento"));
		verify(mockedHandler, times(0)).handlStub(any());
		verify(mockedHandler, times(2)).handleEvent(any());
		verify(mockedHandler, times(0)).doesNothing(any());
		verify(mockedHandler, times(0)).doesNothingWrongEvent(any());
		verify(mockedHandler, times(0)).doesNothingZeroParameter();
	}

	@Test
	void postStubObjectSingleBus() {
		bus.post(new StubObject("stub"));
		bus.post(new StubObject("stub"));
		verify(mockedHandler, times(2)).handlStub(any());
		verify(mockedHandler, times(0)).handleEvent(any());
		verify(mockedHandler, times(0)).doesNothing(any());
		verify(mockedHandler, times(0)).doesNothingWrongEvent(any());
		verify(mockedHandler, times(0)).doesNothingZeroParameter();
	}

	@Test
	void postEventObjectMultiBus() {
		verifyCallsOnBus(anotherBus);
	}

	@Test
	void postEventObjectMultiBusWrongArg() {
		anotherBus.post("Um argumento errado de evento");
		bus.post("Um argumento errado de evento");

		verify(mockedHandler, times(0)).handlStub(any());
		verify(mockedHandler, times(0)).handleEvent(any());
		verify(mockedHandler, times(0)).doesNothing(any());
		verify(mockedHandler, times(0)).doesNothingWrongEvent(any());
		verify(mockedHandler, times(0)).doesNothingZeroParameter();
	}

	@Test
	void register() {
		assertTrue(bus.getInvocations().containsKey(StubObject.class));
		assertTrue(bus.getInvocations().containsKey(EventObject.class));
		assertTrue(anotherBus.getInvocations().containsKey(EventObject.class));
	}

	@Test
	void unregister() {
		bus.unregister(mockedHandler);
		anotherBus.unregister(mockedHandler);

		anotherBus.post(new EventObject("algum evento"));
		bus.post(new EventObject("algum evento"));

		anotherBus.post(new StubObject("algum stub"));
		bus.post(new StubObject("algum stub"));

		verify(mockedHandler, times(0)).handlStub(any());
		verify(mockedHandler, times(0)).handleEvent(any());
		verify(mockedHandler, times(0)).doesNothing(any());
		verify(mockedHandler, times(0)).doesNothingWrongEvent(any());
		verify(mockedHandler, times(0)).doesNothingZeroParameter();

		assertTrue(bus.getInvocations().isEmpty());
		assertTrue(anotherBus.getInvocations().isEmpty());
	}

	class StubObject {

		String name;

		public StubObject(String name) {
			this.name = name;
		}

		public String toString() {
			return "StubObject " + name;
		}
	}

	class EventObject {

		String name;

		public EventObject(String name) {
			this.name = name;
		}

		public String toString() {
			return "EventObject " + name;
		}
	}

	class HandlerClass {

		@Subscribe("GuavaEventBus")
		public void handlStub(StubObject stub) {
			System.out.println("i got stub object " + stub);
		}

		@Subscribe("GuavaEventBus")
		@Subscribe("AnotherGuavaEventBus")
		public void handleEvent(EventObject event) {
			System.out.println("Objeto de evento " + event);
		}

		public void doesNothing(EventObject object) {
			System.out.println("nunca é chamado");
		}

		@Subscribe("WrongGuavaEventBus")
		public void doesNothingWrongEvent(EventObject object) {
			System.out.println("nunca é chamado porque está inscrito no evento errado!");
		}

		@Subscribe("GuavaEventBus")
		@Subscribe("AnotherGuavaEventBus")
		public void doesNothingZeroParameter() {
			System.out.println("esse método será ignorado!");
		}
	}
}