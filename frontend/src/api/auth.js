import api from './index'

export const login = (phone, password, captcha) => api.post('/auth/login', { phone, password, captcha })
export const register = (data) => api.post('/auth/register', data)
export const sendCode = (phone, type = 'register') => api.post('/auth/sendCode', { phone, type })
export const getProfile = () => api.get('/auth/profile')
export const merchantRegister = (data) => api.post('/auth/merchant/register', data)
export const getCaptcha = (phone) => api.post('/auth/captcha', { phone })
export const getLoginFailureCount = (phone) => api.get('/auth/failureCount', { params: { phone } })
export const resetPassword = (phone, password, code) => api.post('/auth/resetPassword', { phone, password, code })
