package kronops.apigenerator;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModel;
import kronops.apigenerator.annotation.*;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

public class Generator {

    public static void run(ClassLoader c, String pkg, String output) throws Exception {


        ConfigurationBuilder conf = ConfigurationBuilder.build().addClassLoader(c).forPackages(pkg);
        ClassLoader cl = Generator.class.getClassLoader();

        Reflections reflections = new Reflections(conf);

        ClientAPIModel apiModel = new ClientAPIModel();


        extractRCPEntities(reflections, apiModel);
        extractRCPEndpoints(reflections, apiModel);


        System.out.println(apiModel.toString());

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);

        // Where do we load the templates from:
        cfg.setClassForTemplateLoading(Generator.class, "templates");
        cfg.setClassLoaderForTemplateLoading(cl, "templates");
        // Some other recommended settings:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        BeansWrapperBuilder wrapperBuilder = new BeansWrapperBuilder(Configuration.VERSION_2_3_28);
        BeansWrapper wrapper = wrapperBuilder.build();
        TemplateHashModel statics = wrapper.getStaticModels();


        Map<String, Object> model = new HashMap<>();

        model.put("model", apiModel);
        model.put("statics", statics);

        Template template = cfg.getTemplate("api.ftl");


        // For the sake of example, also write output into a file:
        Writer fileWriter = new FileWriter(new File(output));
        try {
            template.process(model, fileWriter);
        } finally {
            fileWriter.close();
        }

    }

    private static void extractRCPEndpoints(Reflections reflections, ClientAPIModel apiModel) {
        Set<Class<?>> rpcEndpoints = reflections.getTypesAnnotatedWith(RPCEndpoint.class);

        rpcEndpoints.stream()
                .forEach(rpcEndpoint -> {
                    ClientAPIModel.ApiEndpoint apiEndpoint = new ClientAPIModel.ApiEndpoint();
                    apiEndpoint.setEndpointClass(rpcEndpoint.getName());
                    apiEndpoint.setEndpointName("RPC"+rpcEndpoint.getSimpleName());

                    Arrays.asList(rpcEndpoint.getMethods()).stream()
                            .filter(method -> method.getAnnotation(RPCMethod.class) != null)
                            .forEach(rpcMethod -> {
                                final ClientAPIModel.ApiEndpointMethod apiEndpointMethod = new ClientAPIModel.ApiEndpointMethod();

                                RPCMethod annotation = rpcMethod.getAnnotation(RPCMethod.class);

                                apiEndpointMethod.setMethodName(rpcMethod.getName());

                                if(annotation.returnListOf().equals(SimpleItem.class)){
                                    apiEndpointMethod.setMethodReturnType(RPCUtils.convert(rpcMethod.getReturnType().getSimpleName()));
                                }else{
                                    apiEndpointMethod.setMethodReturnType(RPCUtils.convertList(annotation.returnListOf().getSimpleName()));
                                }

                                Arrays.asList(rpcMethod.getParameters()).stream()
                                        .filter(p -> p.getAnnotation(RPCParam.class) != null)
                                        .forEachOrdered(parameter -> {
                                            RPCParam param = parameter.getAnnotation(RPCParam.class);
                                            if(param.listOf().equals(SimpleItem.class)){
                                                apiEndpointMethod.getMethodParams().add(new ClientAPIModel.ApiEndpointParam(param.value(), RPCUtils.convert(parameter.getType().getSimpleName())));
                                            }else {
                                                apiEndpointMethod.getMethodParams().add(new ClientAPIModel.ApiEndpointParam(param.value(), RPCUtils.convertList(param.listOf().getSimpleName())));
                                            }
                                        });
                                apiEndpoint.getEndpointMethods().add(apiEndpointMethod);
                            });
                    apiModel.getApiEndpoints().add(apiEndpoint);

                });


    }

    private static void extractRCPEntities(Reflections reflections, ClientAPIModel apiModel) {
        Set<Class<?>> rpcEntities = reflections.getTypesAnnotatedWith(RPCEntity.class);


        rpcEntities.stream().forEach(entity -> {
            apiModel.getApiEntities().add(createApiEntity(entity));
        });
    }

    private static ClientAPIModel.ApiEntity createApiEntity(Class<?> entity) {



        ClientAPIModel.ApiEntity apiEntity = new ClientAPIModel.ApiEntity();
        apiEntity.setEntityName(entity.getSimpleName());
        apiEntity.setEnum(entity.isEnum());
        Arrays.asList(entity.getDeclaredFields()).stream()
                .forEach(f -> {
                    ClientAPIModel.TypeAttribute typeAttribute = new ClientAPIModel.TypeAttribute();
                    typeAttribute.setAttributeName(f.getName());

                    RPCParam annotation = f.getAnnotation(RPCParam.class);

                    if (annotation != null && !annotation.listOf().equals(SimpleItem.class)) {
                        typeAttribute.setAttributeType(RPCUtils.convertList(annotation.listOf().getSimpleName()));
                    } else {
                        typeAttribute.setAttributeType(RPCUtils.convert(f.getType().getSimpleName()));
                    }

                    apiEntity.getAttributes().add(typeAttribute);
                });
        return apiEntity;
    }


}
