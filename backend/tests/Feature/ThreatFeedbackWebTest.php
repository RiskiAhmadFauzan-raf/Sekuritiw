<?php

namespace Tests\Feature;

use App\Models\FeedbackAccessLog;
use App\Models\ThreatFeedback;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class ThreatFeedbackWebTest extends TestCase
{
    use RefreshDatabase;

    protected User $analyst;

    protected function setUp(): void
    {
        parent::setUp();

        $this->analyst = User::factory()->create([
            'name'     => 'Analyst Tester',
            'email'    => 'analyst@sekuritiw.id',
            'password' => Hash::make('Password123!'),
            'role'     => 'analyst',
        ]);
    }

    public function test_guest_is_redirected_to_login_when_accessing_dashboard()
    {
        $response = $this->get('/feedback');
        $response->assertRedirect('/login');
    }

    public function test_guest_is_redirected_to_login_when_accessing_root()
    {
        $response = $this->get('/');
        $response->assertRedirect('/login');
    }

    public function test_guest_can_view_login_page()
    {
        $response = $this->get('/login');
        $response->assertStatus(200);
        $response->assertSee('Otentikasi Analis');
        $response->assertSee('Masuk ke Portal Analis');
    }

    public function test_analyst_can_login_with_valid_credentials()
    {
        $response = $this->post('/login', [
            'email'    => 'analyst@sekuritiw.id',
            'password' => 'Password123!',
        ]);

        $response->assertRedirect(route('feedback.index'));
        $this->assertAuthenticatedAs($this->analyst);
    }

    public function test_cannot_login_with_invalid_credentials()
    {
        $response = $this->post('/login', [
            'email'    => 'analyst@sekuritiw.id',
            'password' => 'wrong-password',
        ]);

        $response->assertSessionHasErrors('email');
        $this->assertGuest();
    }

    public function test_analyst_can_logout()
    {
        $response = $this->actingAs($this->analyst)->post('/logout');

        $response->assertRedirect('/login');
        $this->assertGuest();
    }

    public function test_authenticated_analyst_can_view_feedback_web_dashboard()
    {
        ThreatFeedback::create([
            'feedback_type'       => 'FALSE_POSITIVE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'user_reported_label' => 'SAFE',
            'sender'              => 'BANK-BCA',
            'sender_display'      => 'BANK-BCA',
            'package_name'        => 'com.whatsapp',
            'snippet'             => 'Pemberitahuan transfer berhasil.',
            'text_redacted'       => 'Pemberitahuan transfer berhasil.',
            'status'              => 'PENDING',
            'consent_version'     => 'v1',
        ]);

        $response = $this->actingAs($this->analyst)->get('/feedback');

        $response->assertStatus(200);
        $response->assertSee('Daftar Feedback Pendeteksian Ancaman');
        $response->assertSee('BANK-BCA');
        $response->assertSee('False Positive');
        $response->assertSee($this->analyst->name);
        $response->assertSee('Keluar');
    }

    public function test_root_redirects_to_feedback_when_authenticated()
    {
        $response = $this->actingAs($this->analyst)->get('/');
        $response->assertRedirect(route('feedback.index'));
    }

    public function test_can_reveal_content_and_creates_audit_log_with_analyst_id()
    {
        $feedback = ThreatFeedback::create([
            'feedback_type'       => 'FALSE_POSITIVE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'user_reported_label' => 'SAFE',
            'snippet'             => 'Pemberitahuan transaksi rahasia lengkap...',
            'text_redacted'       => 'Pemberitahuan transaksi [NOMOR] Rp[NOMINAL]',
            'status'              => 'PENDING',
            'consent_version'     => 'v1',
        ]);

        $response = $this->actingAs($this->analyst)->postJson("/feedback/{$feedback->id}/reveal");

        $response->assertStatus(200)
            ->assertJson([
                'status'        => 'success',
                'feedback_id'   => $feedback->id,
                'text_redacted' => 'Pemberitahuan transaksi [NOMOR] Rp[NOMINAL]',
            ]);

        $this->assertDatabaseHas('feedback_access_logs', [
            'analyst_id'  => $this->analyst->id,
            'feedback_id' => $feedback->id,
            'action'      => 'REVEAL',
        ]);
    }

    public function test_can_update_feedback_status_and_creates_audit_log()
    {
        $feedback = ThreatFeedback::create([
            'feedback_type'       => 'FALSE_POSITIVE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'user_reported_label' => 'SAFE',
            'status'              => 'PENDING',
            'consent_version'     => 'v1',
        ]);

        $response = $this->actingAs($this->analyst)->patch("/feedback/{$feedback->id}/status", [
            'status' => 'REVIEWED',
        ]);

        $response->assertRedirect();
        $this->assertDatabaseHas('threat_feedbacks', [
            'id'     => $feedback->id,
            'status' => 'REVIEWED',
        ]);

        $this->assertDatabaseHas('feedback_access_logs', [
            'analyst_id'  => $this->analyst->id,
            'feedback_id' => $feedback->id,
            'action'      => 'STATUS_CHANGE',
        ]);
    }

    public function test_export_dataset_only_contains_permitted_fields()
    {
        $feedback = ThreatFeedback::create([
            'feedback_type'       => 'FALSE_POSITIVE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'user_reported_label' => 'SAFE',
            'sender'              => '081234567890',
            'sender_display'      => '+62 812 **** **90',
            'sender_type'         => 'PHONE',
            'package_name'        => 'com.whatsapp',
            'snippet'             => 'Pesan sensitif asli',
            'text_redacted'       => 'Pesan [KODE] tersamar',
            'user_notes'          => 'Catatan pengguna privat',
            'user_note_redacted'  => 'Catatan pengguna privat',
            'reporter_hash'       => 'secret_hash_value',
            'status'              => 'DATASET_EXPORTED',
            'consent_version'     => 'v1',
        ]);

        $response = $this->actingAs($this->analyst)->get('/feedback/export?status=DATASET_EXPORTED');

        $response->assertStatus(200);
        $content = $response->streamedContent();
        $data = json_decode($content, true);

        $this->assertIsArray($data);
        $this->assertNotEmpty($data);

        $item = $data[0];
        // Pastikan hanya field yang diizinkan yang ada
        $this->assertArrayHasKey('text_redacted', $item);
        $this->assertArrayHasKey('label_final', $item);
        $this->assertArrayHasKey('category', $item);
        $this->assertArrayHasKey('app_package', $item);
        $this->assertArrayHasKey('sender_type', $item);

        // Pastikan field terlarang TIDAK ada
        $this->assertArrayNotHasKey('sender_display', $item);
        $this->assertArrayNotHasKey('sender', $item);
        $this->assertArrayNotHasKey('reporter_hash', $item);
        $this->assertArrayNotHasKey('user_notes', $item);
        $this->assertArrayNotHasKey('user_note_redacted', $item);

        // Pastikan export dicatat di audit log dengan ID analis
        $this->assertDatabaseHas('feedback_access_logs', [
            'analyst_id'  => $this->analyst->id,
            'feedback_id' => $feedback->id,
            'action'      => 'EXPORT',
        ]);
    }

    public function test_purge_command_removes_old_data()
    {
        // 1. Data reviewed > 30 hari yang lalu (teks harus dibersihkan)
        $oldReviewed = ThreatFeedback::create([
            'feedback_type'       => 'FALSE_POSITIVE',
            'reason_category'     => 'OFFICIAL_SERVICE',
            'user_reported_label' => 'SAFE',
            'text_redacted'       => 'Teks lama',
            'sender_display'      => 'Pengirim lama',
            'status'              => 'REVIEWED',
            'consent_version'     => 'v1',
        ]);
        $oldReviewed->created_at = now()->subDays(40);
        $oldReviewed->save();

        // 2. Data pending > 90 hari yang lalu (harus dihapus total)
        $oldPending = ThreatFeedback::create([
            'feedback_type'       => 'FALSE_NEGATIVE',
            'reason_category'     => 'NEW_SCAM_PATTERN',
            'user_reported_label' => 'DANGEROUS',
            'status'              => 'PENDING',
            'consent_version'     => 'v1',
        ]);
        $oldPending->created_at = now()->subDays(100);
        $oldPending->save();

        // Jalankan artisan feedback:purge
        $this->artisan('feedback:purge')
            ->assertExitCode(0);

        // Periksa bahwa data reviewed teksnya dikosongkan (retensi minimalisasi)
        $this->assertDatabaseHas('threat_feedbacks', [
            'id'             => $oldReviewed->id,
            'sender_display' => '[DIHAPUS_RETENSI]',
        ]);
        $this->assertNull($oldReviewed->fresh()->text_redacted);

        // Periksa bahwa data pending >90 hari terhapus total
        $this->assertDatabaseMissing('threat_feedbacks', [
            'id' => $oldPending->id,
        ]);
    }
}
