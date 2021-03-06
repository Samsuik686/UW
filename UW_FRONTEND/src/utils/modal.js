import eventBus from './eventBus'
function alertMsg (status, string, timeout, title) {
  eventBus.$emit('notify-me', {
    status: status,
    timeout: timeout,
    data: {
      title: title,
      text: string
    }
  })
}


export const alertDanger = function (string) {
  alertMsg('is-danger', string, 5000, '警告')
};

export const alertSuccess = function (string) {
  alertMsg('is-success', string, 4000, '成功')
};

export const alertInfo = function (string) {
  alertMsg('is-info', string, 4000, '提示')
};
export const alertWarning = function (string) {
  alertMsg('is-warning', string, 5000, '注意')
};
