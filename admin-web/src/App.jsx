import { useState, useEffect } from 'react'
import { createClient } from '@supabase/supabase-js'
import { Users, FileText, CheckCircle, ShieldAlert, UserPlus, Database, Trash2, Edit3, X } from 'lucide-react'

// Supabase configuration
const supabaseUrl = 'https://wabwjiwvtscppensgxrv.supabase.co'
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhYndqaXd2dHNjcHBlbnNneHJ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUwMDMyOTUsImV4cCI6MjEwMDU3OTI5NX0.M-mRVaacAWnPxhS-3esVq91eiYwB53WhH_rWBtvK9JI'
const supabase = createClient(supabaseUrl, supabaseKey)

function App() {
  const [users, setUsers] = useState([])
  const [reports, setReports] = useState([])
  const [resets, setResets] = useState([])
  
  const [loading, setLoading] = useState(true)
  const [editingUserId, setEditingUserId] = useState(null)
  
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    password: '123',
    district: '',
    isModerator: 'false'
  })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    
    // Fetch users
    const { data: usersData } = await supabase.from('users').select('*').order('created_at', { ascending: false })
    if (usersData) setUsers(usersData)

    // Fetch reports
    const { data: reportsData } = await supabase.from('reports').select('*').order('submission_timestamp', { ascending: false })
    if (reportsData) setReports(reportsData)

    // Fetch password resets
    const { data: resetsData } = await supabase.from('password_resets').select('*').order('created_at', { ascending: false })
    if (resetsData) setResets(resetsData)

    setLoading(false)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleEditClick = (user) => {
    setEditingUserId(user.id)
    setFormData({
      fullName: user.full_name,
      username: user.username,
      password: user.password,
      district: user.district || '',
      isModerator: user.is_moderator ? 'true' : 'false'
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const cancelEdit = () => {
    setEditingUserId(null)
    setFormData({ fullName: '', username: '', password: '123', district: '', isModerator: 'false' })
  }

  const handleSaveUser = async (e) => {
    e.preventDefault()
    
    const userData = {
      full_name: formData.fullName,
      username: formData.username.toLowerCase(),
      password: formData.password,
      district: formData.district,
      is_moderator: formData.isModerator === 'true'
    }

    if (editingUserId) {
      // Update existing user
      const { error } = await supabase.from('users').update(userData).eq('id', editingUserId)
      if (error) {
        alert('Hata: ' + error.message)
      } else {
        alert('Kullanıcı başarıyla güncellendi! ✨')
        cancelEdit()
        fetchData()
      }
    } else {
      // Add new user
      userData.id = 'user_' + formData.username + '_' + Math.floor(Math.random() * 1000)
      const { error } = await supabase.from('users').insert([userData])
      if (error) {
        alert('Hata: ' + error.message)
      } else {
        alert('Kullanıcı başarıyla eklendi! ✨')
        cancelEdit()
        fetchData()
      }
    }
  }

  const deleteUser = async (id) => {
    if (confirm('Bu kullanıcıyı silmek istediğinize emin misiniz? (Bu işlem geri alınamaz)')) {
      await supabase.from('users').delete().eq('id', id)
      fetchData()
    }
  }

  return (
    <div className="container">
      <header className="header">
        <div>
          <h1>Dopamine Yönetim</h1>
          <p>Merkezi Sistem ve Kullanıcı Kontrol Paneli</p>
        </div>
        <div className="status-badge">
          <div className="pulse"></div>
          <Database size={16} />
          Supabase Bağlı
        </div>
      </header>

      <div className="stats-grid">
        <div className="card">
          <p style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600, fontSize: '0.85rem', textTransform: 'uppercase' }}>
            <Users size={16} /> Kullanıcılar
          </p>
          <h3>{users.length}</h3>
        </div>
        <div className="card">
          <p style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600, fontSize: '0.85rem', textTransform: 'uppercase' }}>
            <FileText size={16} color="var(--primary-blue)" /> Raporlar
          </p>
          <h3 style={{ color: 'var(--primary-blue)' }}>{reports.length}</h3>
        </div>
        <div className="card">
          <p style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600, fontSize: '0.85rem', textTransform: 'uppercase' }}>
            <CheckCircle size={16} color="var(--status-success)" /> Onaylanan
          </p>
          <h3 style={{ color: 'var(--status-success)' }}>{reports.filter(r => r.status === 'APPROVED').length}</h3>
        </div>
        <div className="card">
          <p style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600, fontSize: '0.85rem', textTransform: 'uppercase' }}>
            <ShieldAlert size={16} color="var(--status-warning)" /> Şifre Sıfırlama
          </p>
          <h3 style={{ color: 'var(--status-warning)' }}>{resets.length}</h3>
        </div>
      </div>

      <div className="main-grid">
        <div className="card h-fit">
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: editingUserId ? 'var(--status-warning)' : 'inherit' }}>
            {editingUserId ? <Edit3 size={20} /> : <UserPlus size={20} />} 
            {editingUserId ? 'Kullanıcıyı Düzenle' : 'Kullanıcı Ekle'}
          </h2>
          <form onSubmit={handleSaveUser}>
            <div className="form-group">
              <label>Ad Soyad</label>
              <input type="text" name="fullName" value={formData.fullName} onChange={handleInputChange} className="form-control" placeholder="Örn: Ayşe Yılmaz" required />
            </div>
            <div className="form-group">
              <label>Kullanıcı Adı</label>
              <input type="text" name="username" value={formData.username} onChange={handleInputChange} className="form-control" placeholder="Örn: ayse" required disabled={!!editingUserId} style={{ opacity: editingUserId ? 0.6 : 1 }} />
            </div>
            <div className="form-group">
              <label>İlçe</label>
              <input type="text" name="district" value={formData.district} onChange={handleInputChange} className="form-control" placeholder="Örn: Kadıköy" required />
            </div>
            <div className="form-group">
              <label>Şifre</label>
              <input type="text" name="password" value={formData.password} onChange={handleInputChange} className="form-control" required />
            </div>
            <div className="form-group">
              <label>Rol</label>
              <select name="isModerator" value={formData.isModerator} onChange={handleInputChange} className="form-control">
                <option value="false">Saha Kullanıcısı</option>
                <option value="true">Moderatör (Admin)</option>
              </select>
            </div>
            
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button type="submit" className="btn" style={{ flex: 1 }}>
                {editingUserId ? <Edit3 size={18} /> : <UserPlus size={18} />} 
                {editingUserId ? 'Güncelle' : 'Ekle & Kaydet'}
              </button>
              
              {editingUserId && (
                <button type="button" onClick={cancelEdit} className="btn" style={{ flex: 1, background: 'rgba(255,255,255,0.1)' }}>
                  <X size={18} /> İptal
                </button>
              )}
            </div>
          </form>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <div className="card">
            <h2>Kayıtlı Kullanıcılar</h2>
            {loading ? <p>Yükleniyor...</p> : (
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Kullanıcı</th>
                      <th>İlçe</th>
                      <th>Şifre</th>
                      <th>Rol</th>
                      <th style={{ textAlign: 'right' }}>İşlemler</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map(u => (
                      <tr key={u.id}>
                        <td>
                          <div style={{ fontWeight: 600 }}>{u.full_name}</div>
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>@{u.username}</div>
                        </td>
                        <td style={{ color: 'var(--primary-blue)', fontWeight: 500 }}>{u.district || '-'}</td>
                        <td style={{ color: 'var(--status-warning)', fontFamily: 'monospace' }}>{u.password}</td>
                        <td>
                          <span className={`badge ${u.is_moderator ? 'badge-admin' : 'badge-user'}`}>
                            {u.is_moderator ? 'Moderatör' : 'Kullanıcı'}
                          </span>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                            <button className="btn btn-danger" style={{ background: 'rgba(255,171,0,0.15)', color: 'var(--status-warning)', borderColor: 'rgba(255,171,0,0.3)' }} onClick={() => handleEditClick(u)}>
                              <Edit3 size={14} /> Düzenle
                            </button>
                            <button className="btn btn-danger" onClick={() => deleteUser(u.id)}>
                              <Trash2 size={14} /> Sil
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {users.length === 0 && <p style={{ padding: '1rem', textAlign: 'center' }}>Henüz kullanıcı yok.</p>}
              </div>
            )}
          </div>
          
          <div className="card">
            <h2>Gelen Raporlar</h2>
            {loading ? <p>Yükleniyor...</p> : (
              <div>
                {reports.length === 0 ? <p>Rapor bulunmuyor.</p> : (
                  reports.map(r => (
                    <div key={r.id} className="list-item flex-between">
                      <div>
                        <div style={{ fontWeight: 600 }}>{r.user_full_name} (@{r.username})</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                          Yeni Üye: {r.new_members_count} | Ev: {r.home_visits_count} | Esnaf: {r.shop_visits_count} | İlçe: {r.district || '-'}
                        </div>
                      </div>
                      <div>
                        <span className="badge" style={{ 
                          backgroundColor: r.status === 'APPROVED' ? 'rgba(0,230,118,0.15)' : (r.status === 'REJECTED' ? 'rgba(255,23,68,0.15)' : 'rgba(255,171,0,0.15)'),
                          color: r.status === 'APPROVED' ? 'var(--status-success)' : (r.status === 'REJECTED' ? 'var(--status-error)' : 'var(--status-warning)')
                        }}>
                          {r.status}
                        </span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
