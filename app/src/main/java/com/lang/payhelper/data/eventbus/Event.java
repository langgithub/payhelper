package com.lang.payhelper.data.eventbus;

import com.lang.payhelper.data.db.entity.SmsCodeRule;
import com.lang.payhelper.feature.backup.ExportResult;

import java.io.File;

public class Event {

    private Event() {
    }

    /**
     * Save template rule event
     */
    public static class TemplateSaveEvent {
        public boolean success;
        public TemplateSaveEvent(boolean success) {
            this.success = success;
        }
    }

    /**
     * Load template rule event
     */
    public static class TemplateLoadEvent {
        public SmsCodeRule template;
        public TemplateLoadEvent(SmsCodeRule template) {
            this.template = template;
        }
    }

    public static class ExportEvent {
        public ExportResult result;
        public File file;
        public ExportEvent(ExportResult result, File file) {
            this.result = result;
            this.file = file;
        }
    }

}
