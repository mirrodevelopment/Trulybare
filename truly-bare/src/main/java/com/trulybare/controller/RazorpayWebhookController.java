package com.trulybare.controller;

import com.trulybare.service.ReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhook")
public class RazorpayWebhookController {

    private final ReturnService returnService;

    /**
     * Razorpay sends webhook events for refund.created / refund.processed / refund.failed
     * Configure the webhook URL in Razorpay dashboard: https://yourdomain.com/api/webhook/razorpay
     */
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        try {
            JSONObject json = new JSONObject(payload);
            String event = json.optString("event", "");
            log.info("Razorpay webhook received: {}", event);

            if (event.startsWith("refund.")) {
                JSONObject refundObj = json
                        .optJSONObject("payload")
                        .optJSONObject("refund")
                        .optJSONObject("entity");

                if (refundObj != null) {
                    String refundId = refundObj.optString("id", "");
                    if (!refundId.isBlank()) {
                        returnService.handleRefundWebhook(refundId, event);
                        log.info("Processed webhook {} for refund {}", event, refundId);
                    }
                }
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage());
            return ResponseEntity.ok("IGNORED");
        }
    }
}
