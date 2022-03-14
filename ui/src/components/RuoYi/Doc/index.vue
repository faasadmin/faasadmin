<template>
  <div>
    <svg-icon icon-class="question" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'faasadminDoc',
  data() {
    return {
      url: 'https://gitee.com/faasadmin/faasadmin'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
