package timeboard.calendar;

/*-
 * #%L
 * webui
 * %%
 * Copyright (C) 2019 Timeboard
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import timeboard.core.api.CalendarService;

import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import timeboard.security.SecurityContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.MultipartConfigElement;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;


@org.osgi.service.component.annotations.Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/calendar",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)

@MultipartConfig(maxFileSize = 3145728)
public class CalendarServlet extends TimeboardServlet {


    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement("/root/.karaf/tmp");

    @Reference
    private CalendarService calendarService;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return CalendarServlet.class.getClassLoader();
    }

    @Override
    protected void handlePost(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

     // String url =String url = request.getParameter("iCalFile");
        String contentType = request.getContentType();
        if(contentType != null && contentType.startsWith("multipart/")){
            request.setAttribute("org.eclipse.jetty.multipartConfig", MULTI_PART_CONFIG); //TODO DIRTY see jetty Request.__MULTIPART_CONFIG_ELEMENT
            for(Part part: request.getParts()) {
                System.out.printf("File %s, %s, %d%n", part.getName(),
                    part.getContentType(), part.getSize());

                try (InputStream is = part.getInputStream()) {
                    // ...
                }
            }
        } else {
           //
        }
        try{
            String url = "";
            this.calendarService.importCalendarFromICS(SecurityContext.getCurrentUser(request), url, url);
        }catch(Exception e){
            //TODO Handle business exceptions
        }

       this.handleGet(request, response, viewModel);

    }

    @Override
    protected void handleGet(HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        viewModel.getViewDatas().put("calendars", this.calendarService.listCalendars());

        viewModel.setTemplate("calendar:calendar.html");

    }


    private void createCalendar(String name, String description, String remoteID){

    }


}
