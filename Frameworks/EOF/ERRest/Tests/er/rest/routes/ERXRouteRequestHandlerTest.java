package er.rest.routes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;

import er.rest.routes.jsr311.DELETE;
import er.rest.routes.jsr311.GET;
import er.rest.routes.jsr311.POST;
import er.rest.routes.jsr311.PUT;
import er.rest.routes.jsr311.Path;
import er.rest.routes.jsr311.Paths;

public class ERXRouteRequestHandlerTest {

	@Test
	public void testRouteForMethodAndPattern() {
		ERXRouteRequestHandler handler = new ERXRouteRequestHandler();
		ERXRoute route1 = new ERXRoute("SomeEntity", "somepattern", ERXRoute.Method.Post);
		handler.addRoute(route1);
		
		// We create this object just to get the constructor-generated url pattern
		ERXRoute route2 = new ERXRoute("SomeEntity", "somepattern");

		ERXRoute route3 = handler.routeForMethodAndPattern(ERXRoute.Method.Post, route2.routePattern().pattern());
		assertTrue(route3 != null);
		assertTrue(route3 == route1);
	}

	// Checks error on verify conflict
	@Test
	public void testVerifyRouteConflict() {
		ERXRouteRequestHandler handler = new ERXRouteRequestHandler();
		ERXRoute route1 = new ERXRoute("SomeEntity", "somepattern");
		handler.addRoute(route1);
		
		ERXRoute route2 = new ERXRoute("SomeEntity", "somepattern", SomeController.class, "someAction");
		
		try {
			handler.verifyRoute(route2);
			fail("Expected IllegalStateException");
		}
		catch (IllegalStateException e) {
			System.out.println(e.getMessage());
		}
	}

	// No error thrown when routes and controller/action are identical
	@Test
	public void testAddRouteSameNoConflict() {
		ERXRouteRequestHandler handler = new ERXRouteRequestHandler();
		ERXRoute route1 = new ERXRoute("SomeEntity", "somepattern", SomeController.class, "someAction");
		System.out.println("route1 = " + route1);
		handler.addRoute(route1);
		
		ERXRoute route2 = new ERXRoute("SomeEntity", "somepattern", SomeController.class, "someAction");
		System.out.println("route2 = " + route2);
		
		handler.addRoute(route2);
		
		// Two identical routes mapping to same controller and action exist.
		assertEquals(handler.routes().count(), 2);
	}

	// Ensures declared methods are added correctly
	@Test
	public void testAddDeclaredMethods() {
		ERXRouteRequestHandler handler = new ERXRouteRequestHandler();
		handler.addDeclaredRoutes("SomeEntity", SomeController.class, true);
		NSArray<ERXRoute> routes = handler.routes();
		assertEquals(routes.count(), 6);

		boolean didFindRoute1 = false;
		boolean didFindRoute2 = false;
		boolean didFindRoute3 = false;
		boolean didFindRoute4 = false;
		boolean didFindRouteXPut = false;
		boolean didFindRouteXDefaultGet = false;
		for (ERXRoute route : routes) {
			System.out.println(route);
			if (route.routePattern().pattern().contains("thing1")) {
				assertEquals(route.method(), ERXRoute.Method.Get);
				// Note that ERRest chops off trailing "Action"
				assertEquals(route.action(), "some");
				didFindRoute1 = true;
			}
			if (route.routePattern().pattern().contains("thing2")) {
				assertEquals(route.method(), ERXRoute.Method.Post);
				assertEquals(route.action(), "someAction2");
				didFindRoute2 = true;
			}
			if (route.routePattern().pattern().contains("thing3")) {
				assertEquals(route.method(), ERXRoute.Method.Put);
				assertEquals(route.action(), "someAction3");
				didFindRoute3 = true;
			}
			if (route.routePattern().pattern().contains("thing4")) {
				assertEquals(route.method(), ERXRoute.Method.Delete);
				assertEquals(route.action(), "someAction4");
				didFindRoute4 = true;
			}
			if (route.routePattern().pattern().contains("thingX")) {
				String routeAction = route.action();
				if (routeAction.equals("someAction3")) {
					assertEquals(route.method(), ERXRoute.Method.Put);
					didFindRouteXPut = true;
				} else if (routeAction.equals("someActionX")) {
					assertEquals(route.method(), ERXRoute.Method.Get);
					didFindRouteXDefaultGet = true;
				}
			}
		}
		assertTrue(didFindRoute1);
		assertTrue(didFindRoute2);
		assertTrue(didFindRoute3);
		assertTrue(didFindRoute4);
		assertTrue(didFindRouteXPut);
		assertTrue(didFindRouteXDefaultGet);
	}
	
	// Throws error where actions have conflicting route declarations
	@Test
	public void testAddDeclaredMethodsWithConflicts() {
		ERXRouteRequestHandler handler = new ERXRouteRequestHandler();
		try {
			handler.addDeclaredRoutes("FaultyEntity", FaultyController.class, true);
			fail("Expected an IllegalStateException");
		}
		catch (IllegalStateException e) {
			;
		}
	}
	
	// Throws error where actions have conflicting route declarations
	@Test
	public void testAddDeclaredMethodsWithConflicts2() {
		ERXRouteRequestHandler handler = new ERXRouteRequestHandler();
		try {
			handler.addDeclaredRoutes("FaultyEntity", FaultyController2.class, true);
			fail("Expected an IllegalStateException");
		}
		catch (IllegalStateException e) {
			;
		}
	}
	
	// A dummy controller for testing with valid declarations
	private static class SomeController extends ERXRouteController {
		public SomeController(WORequest request) {
			super(request);
		}

		
		@GET
		@Path("/somethings/thing1")
		public WOActionResults someAction() {
			return null;
		}

		@POST
		@Path("/somethings/thing2")
		public WOActionResults someAction2() {
			return null;
		}

		@PUT
		@Paths({@Path("/somethings/thing3"), @Path("/somethings/thingX")})
		public WOActionResults someAction3() {
			return null;
		}

		@DELETE
		@Path("/somethings/thing4")
		public WOActionResults someAction4() {
			return null;
		}

		// No http method declaration should default to @GET
		@Path("/somethings/thingX")
		public WOActionResults someActionX() {
			return null;
		}
	}
	
	// A dummy controller for testing
	// This class deliberately has two duplicate declared route conflicts
	private static class FaultyController extends ERXRouteController {
		public FaultyController(WORequest request) {
			super(request);
		}

		@GET
		@Path("/somethings/thing1")
		public WOActionResults someAction() {
			return null;
		}

		// This will default to @GET and so should conflict with "someAction"
		@Path("/somethings/thing1")
		public WOActionResults someAction2() {
			return null;
		}
	}

	// A dummy controller for testing
	// This class deliberately has a duplicate declared route conflict
	private static class FaultyController2 extends ERXRouteController {
		public FaultyController2(WORequest request) {
			super(request);
		}

		@PUT
		@Paths({@Path("/somethings/thing3"), @Path("/somethings/thingX")})
		public WOActionResults someAction3() {
			return null;
		}

		// This should conflict with the same method/path declaration on someAction3
		@PUT
		@Path("/somethings/thingX")
		public WOActionResults someAction4() {
			return null;
		}
	}
}
