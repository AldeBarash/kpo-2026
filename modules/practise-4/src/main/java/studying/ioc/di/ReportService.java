package studying.ioc.di;

import lombok.RequiredArgsConstructor;
import studying.model.Report;
import studying.service.ReportSaver;
import studying.service.ReportSender;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;

@RequiredArgsConstructor
public final class ReportService {
    private final ReportSaver saver;
    private final ReportSender sender;

    public void process(final Report report, final String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Email получателя обязателен"
            );
        }

        saver.save(report);
        sender.send(report, email);
    }
}
