<template>
  <div>
    <svg-icon icon-class="github" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'faasadminGit',
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
