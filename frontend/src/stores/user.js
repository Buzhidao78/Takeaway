import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isRider = ref(localStorage.getItem('isRider') === 'true')
  const role = computed(() => user.value?.role ?? -1)
  const isLogin = computed(() => !!token.value)

  function setUser(u, t) {
    user.value = u
    token.value = t || ''
    if (t) localStorage.setItem('token', t)
    else localStorage.removeItem('token')
    if (u) localStorage.setItem('user', JSON.stringify(u))
    else localStorage.removeItem('user')
  }

  function setRiderStatus(status) {
    isRider.value = status
    localStorage.setItem('isRider', String(status))
  }

  function logout() {
    setUser(null, null)
    setRiderStatus(false)
  }

  async function refreshProfile() {
    console.log('开始刷新用户信息，当前 token:', token.value ? '存在' : '不存在');
    if (token.value) {
      try {
        const res = await api.get('/auth/profile');
        console.log('获取用户信息结果:', res);
        // res 已经是解析后的 data 部分
        setUser(res, token.value);
        console.log('用户信息设置成功:', res);
        return res;
      } catch (e) {
        console.error('获取用户信息失败:', e);
        console.error('错误响应:', e?.response);
        // 如果获取失败，可能 token 已失效，清除本地存储
        if (e?.response?.status === 401 || e?.code === 401) {
          logout();
        }
        throw e;
      }
    } else {
      console.log('无有效 token，跳过用户信息刷新');
    }
  }

  async function checkRiderStatus() {
    try {
      await api.get('/rider/info');
      setRiderStatus(true);
      return true;
    } catch (e) {
      setRiderStatus(false);
      return false;
    }
  }

  return { token, user, role, isLogin, isRider, setUser, setRiderStatus, logout, refreshProfile, checkRiderStatus }
})
