import static org.apache.commons.lang.StringUtils.*
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import Error

/**
 * Scaffolding like controller for exposing RESTful web services
 * for any domain object in both XML and JSON formats.
 *
 * Requires a UrlMapping like the following:
 *	<code>
 *     "/$rest/$controller/$id?"{
 *       controller = "rest"
 *       action = [GET:"show", PUT:"create", POST:"update", DELETE:"delete"]
 *       constraints {
 *	       rest(inList:["rest","json"])
 *       }
 *     }
 * </code>
 */
class RestController {
	 private GrailsDomainClass domainClass
	 private String domainClassName

	/**
	 * Returns a single domain or a list of domains depending
	 * on whether a id parameter is passed or not.
	 * Example:
	 * <li><code>/rest/book/1<code> - returns a single entity
	 * <li><code>/rest/book</code> - returns all entities   n
	 */
	def show = {

        def result
		if(params.id) {
			result = invoke("get", params.id)
		} else {
			 if(!params.max) params.max = 10
			 result = invoke("list", params)
		}
		
		format(result)
	}

	// updates a domain object
	def update = {

		def result
		def domain = invoke("get", params.id)
		if(domain) {
			domain.properties = params
			if(!domain.hasErrors() && domain.save()) {
				result = domain
			} else {
				result = new Error(message: "${domainClassName} could not be saved with id ${params.id}")
			}
		} else {
			result = new Error(message: "${domainClassName} not found with id ${params.id}")
		}
		format(result)		 
	}

	// updates a domain object
	def create = {
		def result
		def domain = InvokerHelper.invokeConstructorOf(domainClass.getClazz(), null)

		def input = ""
		request.inputStream.eachLine {
			input += it
		}
		
		// convert input to name/value pairs
		if(input  && input != '') {
			input.tokenize('&').each{
				def nvp = it.tokenize('=');
				params.put(nvp[0],nvp[1]);
			}
		}
		
        domain.properties = params
        
        if(!domain.hasErrors() && domain.save()) {
            result = domain
        }
        else {
        	log.error domain.errors
        	result = new Error(message: "${domainClassName} could not be created with name of ${params.name}")
        }
        
        format(result)
	}

	// deletes a domain object
	def delete = {
    
		def result = invoke("get", params.id);
	
		if(result) {
			result.delete()
		} else {
			result = new Error(message: "${domainClassName} not found with id ${params.id}")
		}
		
		format(result)
	}
	
	def beforeInterceptor = {
		domainClassName = capitalize(params.domain)
		domainClass = grailsApplication.getArtefact("Domain", domainClassName)
	}
	 
	private invoke(method, parameters) {
		InvokerHelper.invokeStaticMethod(domainClass.getClazz(), method, parameters)
	}
	
	private format(obj) {
		def restType = (params.rest == "rest")?"XML":"JSON"
		render obj."encodeAs$restType"()
	}
	
}