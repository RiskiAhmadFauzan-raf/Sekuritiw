<?php

namespace Tests\Feature;

use App\Models\ThreatRecord;
use App\Models\ThreatFeedback;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ThreatFeedbackTest extends TestCase
{
    use RefreshDatabase;

    public function test_can_submit_false_positive_feedback_with_consent()
    {
        $response = $this->postJson('/api/threats/feedback', [
            'feedback_type'       => 'FALSE_POSITIVE',
            'user_reported_label' => 'SAFE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'user_notes'          => 'Ini SMS resmi dari bank BCA transfer.',
            'device_record_id'    => 'device_rec_123',
            'package_name'        => 'com.google.android.apps.messaging',
            'sender'              => 'BANK-BCA',
            'snippet'             => 'Rekening Anda berhasil didebit Rp 50.000',
            'original_risk_level' => 'DANGEROUS',
            'original_category'   => 'FINANCIAL_SCAM',
            'consent_version'     => 'v1',
        ]);

        $response->assertStatus(201)
            ->assertJson([
                'status' => 'success',
            ]);

        $this->assertDatabaseHas('threat_feedbacks', [
            'feedback_type'   => 'FALSE_POSITIVE',
            'reason_category' => 'OFFICIAL_SERVICE',
            'sender'          => 'BANK-BCA',
            'status'          => 'PENDING',
            'consent_version' => 'v1',
        ]);
    }

    public function test_can_submit_false_negative_feedback()
    {
        $response = $this->postJson('/api/threats/feedback', [
            'feedback_type'       => 'FALSE_NEGATIVE',
            'user_reported_label' => 'DANGEROUS',
            'reason_category'     => 'NEW_SCAM_PATTERN',
            'user_notes'          => 'Ini link penipuan kurir paket APK yang lolos.',
            'device_record_id'    => 'device_rec_456',
            'snippet'             => 'Lihat resi paket Anda di link https://jnt-cek.com/unduh-apk',
            'original_risk_level' => 'SAFE',
            'original_category'   => 'BENIGN',
            'consent_version'     => 'v1',
        ]);

        $response->assertStatus(201);

        $this->assertDatabaseHas('threat_feedbacks', [
            'feedback_type'       => 'FALSE_NEGATIVE',
            'user_reported_label' => 'DANGEROUS',
        ]);
    }

    public function test_cannot_submit_without_consent_version()
    {
        $response = $this->postJson('/api/threats/feedback', [
            'feedback_type'       => 'FALSE_POSITIVE',
            'user_reported_label' => 'SAFE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'snippet'             => 'Pesan tanpa persetujuan',
        ]);

        $response->assertStatus(422);
    }

    public function test_can_retrieve_feedback_stats()
    {
        ThreatFeedback::create([
            'feedback_type'       => 'FALSE_POSITIVE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'user_reported_label' => 'SAFE',
            'status'              => 'PENDING',
            'consent_version'     => 'v1',
        ]);

        ThreatFeedback::create([
            'feedback_type'       => 'FALSE_NEGATIVE',
            'reason_category'     => 'NEW_SCAM_PATTERN',
            'user_reported_label' => 'DANGEROUS',
            'status'              => 'PENDING',
            'consent_version'     => 'v1',
        ]);

        $response = $this->getJson('/api/threats/feedback/stats');

        $response->assertStatus(200)
            ->assertJson([
                'status'  => 'success',
                'summary' => [
                    'total_feedbacks' => 2,
                    'false_positives' => 1,
                    'false_negatives' => 1,
                    'pending_review'  => 2,
                ],
            ]);
    }

    public function test_validation_fails_on_invalid_feedback_type()
    {
        $response = $this->postJson('/api/threats/feedback', [
            'feedback_type'   => 'INVALID_TYPE',
            'reason_category' => 'SOMETHING',
            'consent_version' => 'v1',
        ]);

        $response->assertStatus(422);
    }

    public function test_cannot_submit_duplicate_feedback_for_same_record()
    {
        $payload = [
            'feedback_type'       => 'FALSE_POSITIVE',
            'user_reported_label' => 'SAFE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'device_record_id'    => 'device_rec_duplicate_test',
            'snippet'             => 'Pesan uji coba duplikasi',
            'consent_version'     => 'v1',
        ];

        // Pengiriman pertama -> 201 Created
        $response1 = $this->postJson('/api/threats/feedback', $payload);
        $response1->assertStatus(201);

        // Pengiriman kedua untuk record yang sama -> 200 dengan pesan sudah pernah dicatat
        $response2 = $this->postJson('/api/threats/feedback', $payload);
        $response2->assertStatus(200)
            ->assertJson([
                'status'  => 'success',
                'message' => 'Laporan untuk pesan ini sudah pernah dicatat sebelumnya.',
            ]);

        // Pastikan record di database tetap hanya 1 (tidak duplikat)
        $this->assertEquals(1, ThreatFeedback::where('device_record_id', 'device_rec_duplicate_test')->count());
    }

    public function test_submitting_whatsapp_feedback_automatically_masks_contact_sender()
    {
        $response = $this->postJson('/api/threats/feedback', [
            'feedback_type'       => 'FALSE_NEGATIVE',
            'user_reported_label' => 'DANGEROUS',
            'reason_category'     => 'APK_SCAM_PACKAGE',
            'user_notes'          => 'Pesan dari kontak WA menyuruh download APK',
            'device_record_id'    => 'device_rec_wa_test',
            'package_name'        => 'com.whatsapp',
            'sender'              => 'Jonatan  IF',
            'sender_type'         => 'ALPHANUMERIC', // Mobile mistakenly sent ALPHANUMERIC
            'snippet'             => 'Silakan klik link unduh paket https://kurir-update.com/apk',
            'consent_version'     => 'v1',
        ]);

        $response->assertStatus(201);

        $this->assertDatabaseHas('threat_feedbacks', [
            'device_record_id' => 'device_rec_wa_test',
            'package_name'     => 'com.whatsapp',
            'sender'           => 'J***n I*',
            'sender_display'   => 'J***n I*',
            'sender_type'      => 'CONTACT',
        ]);
    }
}

