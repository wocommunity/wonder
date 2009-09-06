package er.rest.routes;

import com.webobjects.appserver.WOActionResults;

/**
 * <p>
 * If you return a component from ERXRouteController that implements the IERXRouteComponent interface, the route
 * controller will attempt to find set-methods on your component that correspond to the current route keys. For
 * instance, if you have a {person:Person} in your route, ERXRouteController will attempt to call setPerson(Person) on
 * your component as long as the setPerson method has a @ERXRouteParameter annotation.
 * </p>
 * 
 * <p>
 * The system will look for a String variant of the method first (to avoid faulting if you don't want to trigger a
 * fault). If a String method is not found, it will look for the typed version, which will trigger a fault of the object
 * if the method is found.
 * </p>
 * 
 * <b>Example</b>
 * <pre>
 * route (super-contrived): /person/{person:Person}/company/{company:Company}/task/{task:Task}
 * </pre>
 * 
 * <pre>
 * public class TasksController extends ERXRouteController {
 *     public WOActionResults viewAction() {
 *         if (format() == ERXRestFormat.HTML) {
 *             return pageWithName(TaskComponent.class);
 *         }
 *         return response(...);
 *     }
 * }
 * </pre>
 * 
 * <pre>
 * public class TaskComponent extends ERXComponent implements IERXRouteComponent {
 *     &#064;ERXRouteParameter
 *     public void setCompany(String companyID) {
 *         ...
 *     }
 *     
 *     &#064;ERXRouteParameter
 *     public void setTask(Task task) {
 *         ...
 *     }
 *     
 *     public void setPerson(Person person) {
 *         ...
 *     }
 * }
 * </pre>
 * 
 * <p>
 * In the above example, setCompany will be called, passing in the company ID from the URL. setTask will be called,
 * passing in the actual Task object, and setPerson will NOT be called because it is not annotated @ERXRouteParameter
 * 
 * @author mschrag
 */
public interface IERXRouteComponent extends WOActionResults {

}
