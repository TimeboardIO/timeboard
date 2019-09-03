package kronops.rpcbridge;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component(
        service=Servlet.class,
        property= "osgi.http.whiteboard.servlet.pattern=/rpc",
        scope=ServiceScope.PROTOTYPE)
public class RPCHttpBridge extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private BundleContext bundleContext;

    static{
        MAPPER.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ParamsList.class, new RPCRequestParamsDeserializer());
        MAPPER.registerModule(module);
    }


    @Activate
    private void init(BundleContext bundleContext){
        System.out.println("Init Rpc Bridge");
        this.bundleContext=  bundleContext;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        RPCResponse response = new RPCResponse();

        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        Map<String, Object> requestMap = MAPPER.readValue(body, Map.class);

        RPCError error = new RPCError();

        try {
            response.setId(Long.valueOf(String.valueOf(requestMap.get("id"))));
            response.setJsonrpc((String) requestMap.get("jsonrpc"));
            String className = ((String)requestMap.get("method")).split("#")[0];
            String methodName = ((String)requestMap.get("method")).split("#")[1];

            Class<?> methodClass = Class.forName(className);

            ServiceReference reference = this.bundleContext.getServiceReference(methodClass);
            Object instance = this.bundleContext.getService(reference);

            Method method = Arrays.asList(methodClass.getMethods())
                    .stream().filter(m -> m.getName().equals(methodName)).collect(Collectors.toList()).get(0);


            List<Class<?>> targetMethodParams = Arrays.asList(method.getParameterTypes());
            ParamsList castedParameters = MAPPER
                    .reader()
                    .forType(ParamsList.class)
                    .withAttribute("targetParams", targetMethodParams)
                    .readValue(body);

            Object res = null;
            if (castedParameters.isEmpty()) {
                res = method.invoke(instance);
            } else {
                res = method.invoke(instance, castedParameters.toArray());
            }

            response.setResult(res);
        } catch (Exception e) {
            error.getData().add(e);
            response.setError(error);
        }finally {
        }

        MAPPER.writeValue(resp.getWriter(), response);
    }

    public static class RPCError {
        private final List<Exception> data;

        public RPCError() {
            data = new ArrayList<>();
        }

        public int getCode() {
            if(!data.isEmpty()){
                return -32603;
            }else {
                return -32000;
            }
        }


        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            this.getData().stream().forEach(builder::append);
            return builder.toString();
        }


        public List<Exception> getData() {
            return data;
        }

    }


    public static class RPCResponse {
        private   Long id;
        private   String jsonrpc;
        private Object result;
        private RPCError error;

        public void setId(Long id) {
            this.id = id;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public RPCError getError() {
            return error;
        }

        public void setError(RPCError error) {
            this.error = error;
        }

        public Long getId() {
            return id;
        }


        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public String getJsonrpc() {
            return jsonrpc;
        }
    }

    public static class RPCRequest {
        private Long id;
        private String method;
        private String jsonrpc;
        private List<Object> params;

        public List<Object> getParams() {
            return params;
        }

        public void setParams(List<Object> params) {
            this.params = params;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }




        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }
    }

    public static class ParamsList extends ArrayList<Object>{

    }

    public static class RPCRequestParamsDeserializer extends StdDeserializer<ParamsList> {

        public RPCRequestParamsDeserializer(){
            this(null);
        }

        @Override
        public ParamsList deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

            List<Class<?>> targetParams = (List<Class<?>>) deserializationContext.getAttribute("targetParams");

            ParamsList req = new ParamsList();

            JsonNode node = jp.getCodec().readTree(jp);

            JsonNode paramsNode = node.get("params");

            int cpt = 0;
            Iterator<JsonNode> it = paramsNode.iterator();
            while (it.hasNext()){
                JsonNode value = it.next();
                req.add(MAPPER.readValue(value.toString(), targetParams.get(cpt)));
                cpt++;
            }



            return req;
        }

        protected RPCRequestParamsDeserializer(Class<?> vc) {
            super(vc);
        }


    }
}
